package net.minecraft.client.renderer.entity;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import com.mojang.math.Vector3f;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.Items;
import net.minecraft.world.scores.Team;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public abstract class LivingEntityRenderer<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements RenderLayerParent<T, M> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final float EYE_BED_OFFSET = 0.1F;
    protected M model;
    protected final List<RenderLayer<T, M>> layers = Lists.newArrayList();

    public LivingEntityRenderer(EntityRendererProvider.Context param0, M param1, float param2) {
        super(param0);
        this.model = param1;
        this.shadowRadius = param2;
    }

    protected final boolean addLayer(RenderLayer<T, M> param0) {
        return this.layers.add(param0);
    }

    @Override
    public M getModel() {
        return this.model;
    }

    public void render(T param0, float param1, float param2, PoseStack param3, MultiBufferSource param4, int param5) {
        param3.pushPose();
        this.model.attackTime = this.getAttackAnim(param0, param2);
        this.model.riding = param0.isPassenger();
        this.model.young = param0.isBaby();
        float var0 = Mth.rotLerp(param2, param0.yBodyRotO, param0.yBodyRot);
        float var1 = Mth.rotLerp(param2, param0.yHeadRotO, param0.yHeadRot);
        float var2 = var1 - var0;
        if (param0.isPassenger() && param0.getVehicle() instanceof LivingEntity var3) {
            var0 = Mth.rotLerp(param2, var3.yBodyRotO, var3.yBodyRot);
            var2 = var1 - var0;
            float var4 = Mth.wrapDegrees(var2);
            if (var4 < -85.0F) {
                var4 = -85.0F;
            }

            if (var4 >= 85.0F) {
                var4 = 85.0F;
            }

            var0 = var1 - var4;
            if (var4 * var4 > 2500.0F) {
                var0 += var4 * 0.2F;
            }

            var2 = var1 - var0;
        }

        float var5 = Mth.lerp(param2, param0.xRotO, param0.getXRot());
        if (isEntityUpsideDown(param0)) {
            var5 *= -1.0F;
            var2 *= -1.0F;
        }

        if (param0.hasPose(Pose.SLEEPING)) {
            Direction var6 = param0.getBedOrientation();
            if (var6 != null) {
                float var7 = param0.getEyeHeight(Pose.STANDING) - 0.1F;
                param3.translate((double)((float)(-var6.getStepX()) * var7), 0.0, (double)((float)(-var6.getStepZ()) * var7));
            }
        }

        float var8 = this.getBob(param0, param2);
        this.setupRotations(param0, param3, var8, var0, param2);
        param3.scale(-1.0F, -1.0F, 1.0F);
        this.scale(param0, param3, param2);
        param3.translate(0.0, -1.501F, 0.0);
        float var9 = 0.0F;
        float var10 = 0.0F;
        if (!param0.isPassenger() && param0.isAlive()) {
            var9 = Mth.lerp(param2, param0.animationSpeedOld, param0.animationSpeed);
            var10 = param0.animationPosition - param0.animationSpeed * (1.0F - param2);
            if (param0.isBaby()) {
                var10 *= 3.0F;
            }

            if (var9 > 1.0F) {
                var9 = 1.0F;
            }
        }

        this.model.prepareMobModel(param0, var10, var9, param2);
        this.model.setupAnim(param0, var10, var9, var8, var2, var5);
        Minecraft var11 = Minecraft.getInstance();
        boolean var12 = this.isBodyVisible(param0);
        boolean var13 = !var12 && !param0.isInvisibleTo(var11.player);
        boolean var14 = var11.shouldEntityAppearGlowing(param0);
        RenderType var15 = this.getRenderType(param0, var12, var13, var14);
        if (var15 != null) {
            VertexConsumer var16 = param4.getBuffer(var15);
            int var17 = getOverlayCoords(param0, this.getWhiteOverlayProgress(param0, param2));
            this.model.renderToBuffer(param3, var16, param5, var17, 1.0F, 1.0F, 1.0F, var13 ? 0.15F : 1.0F);
        }

        if (!param0.isSpectator()) {
            for(RenderLayer<T, M> var18 : this.layers) {
                var18.render(param3, param4, param5, param0, var10, var9, param2, var8, var2, var5);
            }
        }

        param3.popPose();
        super.render(param0, param1, param2, param3, param4, param5);
    }

    @Nullable
    protected RenderType getRenderType(T param0, boolean param1, boolean param2, boolean param3) {
        ResourceLocation var0 = this.getTextureLocation(param0);
        if (param2) {
            return RenderType.itemEntityTranslucentCull(var0);
        } else if (param1) {
            return this.model.renderType(var0);
        } else {
            return param3 ? RenderType.outline(var0) : null;
        }
    }

    public static int getOverlayCoords(LivingEntity param0, float param1) {
        return OverlayTexture.pack(OverlayTexture.u(param1), OverlayTexture.v(param0.hurtTime > 0 || param0.deathTime > 0));
    }

    protected boolean isBodyVisible(T param0) {
        return !param0.isInvisible();
    }

    private static float sleepDirectionToRotation(Direction param0) {
        switch(param0) {
            case SOUTH:
                return 90.0F;
            case WEST:
                return 0.0F;
            case NORTH:
                return 270.0F;
            case EAST:
                return 180.0F;
            default:
                return 0.0F;
        }
    }

    protected boolean isShaking(T param0) {
        return param0.isFullyFrozen();
    }

    protected void setupRotations(T param0, PoseStack param1, float param2, float param3, float param4) {
        if (param0.getItemBySlot(EquipmentSlot.HEAD).is(Items.BARREL) && param0.isCrouching()) {
            param3 = 0.0F;
        } else {
            if (this.isShaking(param0)) {
                param3 += (float)(Math.cos((double)param0.tickCount * 3.25) * Math.PI * 0.4F);
            }

            if (!param0.hasPose(Pose.SLEEPING)) {
                param1.mulPose(Vector3f.YP.rotationDegrees(180.0F - param3));
            }

            if (param0.deathTime > 0) {
                float var0 = ((float)param0.deathTime + param4 - 1.0F) / 20.0F * 1.6F;
                var0 = Mth.sqrt(var0);
                if (var0 > 1.0F) {
                    var0 = 1.0F;
                }

                param1.mulPose(Vector3f.ZP.rotationDegrees(var0 * this.getFlipDegrees(param0)));
            } else if (param0.isAutoSpinAttack()) {
                param1.mulPose(Vector3f.XP.rotationDegrees(-90.0F - param0.getXRot()));
                param1.mulPose(Vector3f.YP.rotationDegrees(((float)param0.tickCount + param4) * -75.0F));
            } else if (param0.hasPose(Pose.SLEEPING)) {
                Direction var1 = param0.getBedOrientation();
                float var2 = var1 != null ? sleepDirectionToRotation(var1) : param3;
                param1.mulPose(Vector3f.YP.rotationDegrees(var2));
                param1.mulPose(Vector3f.ZP.rotationDegrees(this.getFlipDegrees(param0)));
                param1.mulPose(Vector3f.YP.rotationDegrees(270.0F));
            } else if (isEntityUpsideDown(param0)) {
                param1.translate(0.0, (double)(param0.getBbHeight() + 0.1F), 0.0);
                param1.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
            }

        }
    }

    protected float getAttackAnim(T param0, float param1) {
        return param0.getAttackAnim(param1);
    }

    protected float getBob(T param0, float param1) {
        return (float)param0.tickCount + param1;
    }

    protected float getFlipDegrees(T param0) {
        return 90.0F;
    }

    protected float getWhiteOverlayProgress(T param0, float param1) {
        return 0.0F;
    }

    protected void scale(T param0, PoseStack param1, float param2) {
    }

    protected boolean shouldShowName(T param0) {
        double var0 = this.entityRenderDispatcher.distanceToSqr(param0);
        float var1 = param0.isDiscrete() ? 32.0F : 64.0F;
        if (var0 >= (double)(var1 * var1)) {
            return false;
        } else {
            Minecraft var2 = Minecraft.getInstance();
            LocalPlayer var3 = var2.player;
            boolean var4 = !param0.isInvisibleTo(var3);
            if (param0 != var3) {
                Team var5 = param0.getTeam();
                Team var6 = var3.getTeam();
                if (var5 != null) {
                    Team.Visibility var7 = var5.getNameTagVisibility();
                    switch(var7) {
                        case ALWAYS:
                            return var4;
                        case NEVER:
                            return false;
                        case HIDE_FOR_OTHER_TEAMS:
                            return var6 == null ? var4 : var5.isAlliedTo(var6) && (var5.canSeeFriendlyInvisibles() || var4);
                        case HIDE_FOR_OWN_TEAM:
                            return var6 == null ? var4 : !var5.isAlliedTo(var6) && var4;
                        default:
                            return true;
                    }
                }
            }

            return Minecraft.renderNames() && param0 != var2.getCameraEntity() && var4 && !param0.isVehicle();
        }
    }

    public static boolean isEntityUpsideDown(LivingEntity param0) {
        if (param0 instanceof Player || param0.hasCustomName()) {
            String var0 = ChatFormatting.stripFormatting(param0.getName().getString());
            if ("Dinnerbone".equals(var0) || "Grumm".equals(var0)) {
                return !(param0 instanceof Player) || ((Player)param0).isModelPartShown(PlayerModelPart.CAPE);
            }
        }

        return param0 instanceof Pig && param0.getVehicle() instanceof Player;
    }
}

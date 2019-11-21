package net.minecraft.client.renderer.entity;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import java.util.List;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.scores.Team;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public abstract class LivingEntityRenderer<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements RenderLayerParent<T, M> {
    private static final Logger LOGGER = LogManager.getLogger();
    protected M model;
    protected final List<RenderLayer<T, M>> layers = Lists.newArrayList();

    public LivingEntityRenderer(EntityRenderDispatcher param0, M param1, float param2) {
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
        if (param0.isPassenger() && param0.getVehicle() instanceof LivingEntity) {
            LivingEntity var3 = (LivingEntity)param0.getVehicle();
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

        float var5 = Mth.lerp(param2, param0.xRotO, param0.xRot);
        if (param0.getPose() == Pose.SLEEPING) {
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
        boolean var11 = this.isVisible(param0, false);
        boolean var12 = !var11 && !param0.isInvisibleTo(Minecraft.getInstance().player);
        this.model.setupAnim(param0, var10, var9, var8, var2, var5);
        if (var11 || var12) {
            ResourceLocation var13 = this.getTextureLocation(param0);
            VertexConsumer var14 = param4.getBuffer(var12 ? RenderType.entityTranslucent(var13) : this.model.renderType(var13));
            this.model
                .renderToBuffer(
                    param3, var14, param5, getOverlayCoords(param0, this.getWhiteOverlayProgress(param0, param2)), 1.0F, 1.0F, 1.0F, var12 ? 0.15F : 1.0F
                );
        }

        if (!param0.isSpectator()) {
            for(RenderLayer<T, M> var15 : this.layers) {
                var15.render(param3, param4, param5, param0, var10, var9, param2, var8, var2, var5);
            }
        }

        param3.popPose();
        super.render(param0, param1, param2, param3, param4, param5);
    }

    public static int getOverlayCoords(LivingEntity param0, float param1) {
        return OverlayTexture.pack(OverlayTexture.u(param1), OverlayTexture.v(param0.hurtTime > 0 || param0.deathTime > 0));
    }

    protected boolean isVisible(T param0, boolean param1) {
        return !param0.isInvisible() || param1;
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

    protected void setupRotations(T param0, PoseStack param1, float param2, float param3, float param4) {
        Pose var0 = param0.getPose();
        if (var0 != Pose.SLEEPING) {
            param1.mulPose(Vector3f.YP.rotationDegrees(180.0F - param3));
        }

        if (param0.deathTime > 0) {
            float var1 = ((float)param0.deathTime + param4 - 1.0F) / 20.0F * 1.6F;
            var1 = Mth.sqrt(var1);
            if (var1 > 1.0F) {
                var1 = 1.0F;
            }

            param1.mulPose(Vector3f.ZP.rotationDegrees(var1 * this.getFlipDegrees(param0)));
        } else if (param0.isAutoSpinAttack()) {
            param1.mulPose(Vector3f.XP.rotationDegrees(-90.0F - param0.xRot));
            param1.mulPose(Vector3f.YP.rotationDegrees(((float)param0.tickCount + param4) * -75.0F));
        } else if (var0 == Pose.SLEEPING) {
            Direction var2 = param0.getBedOrientation();
            float var3 = var2 != null ? sleepDirectionToRotation(var2) : param3;
            param1.mulPose(Vector3f.YP.rotationDegrees(var3));
            param1.mulPose(Vector3f.ZP.rotationDegrees(this.getFlipDegrees(param0)));
            param1.mulPose(Vector3f.YP.rotationDegrees(270.0F));
        } else if (param0.hasCustomName() || param0 instanceof Player) {
            String var4 = ChatFormatting.stripFormatting(param0.getName().getString());
            if (("Dinnerbone".equals(var4) || "Grumm".equals(var4)) && (!(param0 instanceof Player) || ((Player)param0).isModelPartShown(PlayerModelPart.CAPE))
                )
             {
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
}

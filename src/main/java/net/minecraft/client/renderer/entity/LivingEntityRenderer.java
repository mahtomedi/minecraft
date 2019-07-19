package net.minecraft.client.renderer.entity;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.MemoryTracker;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.Direction;
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
    private static final DynamicTexture WHITE_TEXTURE = Util.make(new DynamicTexture(16, 16, false), param0 -> {
        param0.getPixels().untrack();

        for(int var0 = 0; var0 < 16; ++var0) {
            for(int var1 = 0; var1 < 16; ++var1) {
                param0.getPixels().setPixelRGBA(var1, var0, -1);
            }
        }

        param0.upload();
    });
    protected M model;
    protected final FloatBuffer tintBuffer = MemoryTracker.createFloatBuffer(4);
    protected final List<RenderLayer<T, M>> layers = Lists.newArrayList();
    protected boolean onlySolidLayers;

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

    public void render(T param0, double param1, double param2, double param3, float param4, float param5) {
        GlStateManager.pushMatrix();
        GlStateManager.disableCull();
        this.model.attackTime = this.getAttackAnim(param0, param5);
        this.model.riding = param0.isPassenger();
        this.model.young = param0.isBaby();

        try {
            float var0 = Mth.rotLerp(param5, param0.yBodyRotO, param0.yBodyRot);
            float var1 = Mth.rotLerp(param5, param0.yHeadRotO, param0.yHeadRot);
            float var2 = var1 - var0;
            if (param0.isPassenger() && param0.getVehicle() instanceof LivingEntity) {
                LivingEntity var3 = (LivingEntity)param0.getVehicle();
                var0 = Mth.rotLerp(param5, var3.yBodyRotO, var3.yBodyRot);
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

            float var5 = Mth.lerp(param5, param0.xRotO, param0.xRot);
            this.setupPosition(param0, param1, param2, param3);
            float var6 = this.getBob(param0, param5);
            this.setupRotations(param0, var6, var0, param5);
            float var7 = this.setupScale(param0, param5);
            float var8 = 0.0F;
            float var9 = 0.0F;
            if (!param0.isPassenger() && param0.isAlive()) {
                var8 = Mth.lerp(param5, param0.animationSpeedOld, param0.animationSpeed);
                var9 = param0.animationPosition - param0.animationSpeed * (1.0F - param5);
                if (param0.isBaby()) {
                    var9 *= 3.0F;
                }

                if (var8 > 1.0F) {
                    var8 = 1.0F;
                }
            }

            GlStateManager.enableAlphaTest();
            this.model.prepareMobModel(param0, var9, var8, param5);
            this.model.setupAnim(param0, var9, var8, var6, var2, var5, var7);
            if (this.solidRender) {
                boolean var10 = this.setupSolidState(param0);
                GlStateManager.enableColorMaterial();
                GlStateManager.setupSolidRenderingTextureCombine(this.getTeamColor(param0));
                if (!this.onlySolidLayers) {
                    this.renderModel(param0, var9, var8, var6, var2, var5, var7);
                }

                if (!param0.isSpectator()) {
                    this.renderLayers(param0, var9, var8, param5, var6, var2, var5, var7);
                }

                GlStateManager.tearDownSolidRenderingTextureCombine();
                GlStateManager.disableColorMaterial();
                if (var10) {
                    this.tearDownSolidState();
                }
            } else {
                boolean var11 = this.setupOverlayColor(param0, param5);
                this.renderModel(param0, var9, var8, var6, var2, var5, var7);
                if (var11) {
                    this.teardownOverlayColor();
                }

                GlStateManager.depthMask(true);
                if (!param0.isSpectator()) {
                    this.renderLayers(param0, var9, var8, param5, var6, var2, var5, var7);
                }
            }

            GlStateManager.disableRescaleNormal();
        } catch (Exception var19) {
            LOGGER.error("Couldn't render entity", (Throwable)var19);
        }

        GlStateManager.activeTexture(GLX.GL_TEXTURE1);
        GlStateManager.enableTexture();
        GlStateManager.activeTexture(GLX.GL_TEXTURE0);
        GlStateManager.enableCull();
        GlStateManager.popMatrix();
        super.render(param0, param1, param2, param3, param4, param5);
    }

    public float setupScale(T param0, float param1) {
        GlStateManager.enableRescaleNormal();
        GlStateManager.scalef(-1.0F, -1.0F, 1.0F);
        this.scale(param0, param1);
        float var0 = 0.0625F;
        GlStateManager.translatef(0.0F, -1.501F, 0.0F);
        return 0.0625F;
    }

    protected boolean setupSolidState(T param0) {
        GlStateManager.disableLighting();
        GlStateManager.activeTexture(GLX.GL_TEXTURE1);
        GlStateManager.disableTexture();
        GlStateManager.activeTexture(GLX.GL_TEXTURE0);
        return true;
    }

    protected void tearDownSolidState() {
        GlStateManager.enableLighting();
        GlStateManager.activeTexture(GLX.GL_TEXTURE1);
        GlStateManager.enableTexture();
        GlStateManager.activeTexture(GLX.GL_TEXTURE0);
    }

    protected void renderModel(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        boolean var0 = this.isVisible(param0);
        boolean var1 = !var0 && !param0.isInvisibleTo(Minecraft.getInstance().player);
        if (var0 || var1) {
            if (!this.bindTexture(param0)) {
                return;
            }

            if (var1) {
                GlStateManager.setProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
            }

            this.model.render(param0, param1, param2, param3, param4, param5, param6);
            if (var1) {
                GlStateManager.unsetProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
            }
        }

    }

    protected boolean isVisible(T param0) {
        return !param0.isInvisible() || this.solidRender;
    }

    protected boolean setupOverlayColor(T param0, float param1) {
        return this.setupOverlayColor(param0, param1, true);
    }

    protected boolean setupOverlayColor(T param0, float param1, boolean param2) {
        float var0 = param0.getBrightness();
        int var1 = this.getOverlayColor(param0, var0, param1);
        boolean var2 = (var1 >> 24 & 0xFF) > 0;
        boolean var3 = param0.hurtTime > 0 || param0.deathTime > 0;
        if (!var2 && !var3) {
            return false;
        } else if (!var2 && !param2) {
            return false;
        } else {
            GlStateManager.activeTexture(GLX.GL_TEXTURE0);
            GlStateManager.enableTexture();
            GlStateManager.texEnv(8960, 8704, GLX.GL_COMBINE);
            GlStateManager.texEnv(8960, GLX.GL_COMBINE_RGB, 8448);
            GlStateManager.texEnv(8960, GLX.GL_SOURCE0_RGB, GLX.GL_TEXTURE0);
            GlStateManager.texEnv(8960, GLX.GL_SOURCE1_RGB, GLX.GL_PRIMARY_COLOR);
            GlStateManager.texEnv(8960, GLX.GL_OPERAND0_RGB, 768);
            GlStateManager.texEnv(8960, GLX.GL_OPERAND1_RGB, 768);
            GlStateManager.texEnv(8960, GLX.GL_COMBINE_ALPHA, 7681);
            GlStateManager.texEnv(8960, GLX.GL_SOURCE0_ALPHA, GLX.GL_TEXTURE0);
            GlStateManager.texEnv(8960, GLX.GL_OPERAND0_ALPHA, 770);
            GlStateManager.activeTexture(GLX.GL_TEXTURE1);
            GlStateManager.enableTexture();
            GlStateManager.texEnv(8960, 8704, GLX.GL_COMBINE);
            GlStateManager.texEnv(8960, GLX.GL_COMBINE_RGB, GLX.GL_INTERPOLATE);
            GlStateManager.texEnv(8960, GLX.GL_SOURCE0_RGB, GLX.GL_CONSTANT);
            GlStateManager.texEnv(8960, GLX.GL_SOURCE1_RGB, GLX.GL_PREVIOUS);
            GlStateManager.texEnv(8960, GLX.GL_SOURCE2_RGB, GLX.GL_CONSTANT);
            GlStateManager.texEnv(8960, GLX.GL_OPERAND0_RGB, 768);
            GlStateManager.texEnv(8960, GLX.GL_OPERAND1_RGB, 768);
            GlStateManager.texEnv(8960, GLX.GL_OPERAND2_RGB, 770);
            GlStateManager.texEnv(8960, GLX.GL_COMBINE_ALPHA, 7681);
            GlStateManager.texEnv(8960, GLX.GL_SOURCE0_ALPHA, GLX.GL_PREVIOUS);
            GlStateManager.texEnv(8960, GLX.GL_OPERAND0_ALPHA, 770);
            ((Buffer)this.tintBuffer).position(0);
            if (var3) {
                this.tintBuffer.put(1.0F);
                this.tintBuffer.put(0.0F);
                this.tintBuffer.put(0.0F);
                this.tintBuffer.put(0.3F);
            } else {
                float var4 = (float)(var1 >> 24 & 0xFF) / 255.0F;
                float var5 = (float)(var1 >> 16 & 0xFF) / 255.0F;
                float var6 = (float)(var1 >> 8 & 0xFF) / 255.0F;
                float var7 = (float)(var1 & 0xFF) / 255.0F;
                this.tintBuffer.put(var5);
                this.tintBuffer.put(var6);
                this.tintBuffer.put(var7);
                this.tintBuffer.put(1.0F - var4);
            }

            ((Buffer)this.tintBuffer).flip();
            GlStateManager.texEnv(8960, 8705, this.tintBuffer);
            GlStateManager.activeTexture(GLX.GL_TEXTURE2);
            GlStateManager.enableTexture();
            GlStateManager.bindTexture(WHITE_TEXTURE.getId());
            GlStateManager.texEnv(8960, 8704, GLX.GL_COMBINE);
            GlStateManager.texEnv(8960, GLX.GL_COMBINE_RGB, 8448);
            GlStateManager.texEnv(8960, GLX.GL_SOURCE0_RGB, GLX.GL_PREVIOUS);
            GlStateManager.texEnv(8960, GLX.GL_SOURCE1_RGB, GLX.GL_TEXTURE1);
            GlStateManager.texEnv(8960, GLX.GL_OPERAND0_RGB, 768);
            GlStateManager.texEnv(8960, GLX.GL_OPERAND1_RGB, 768);
            GlStateManager.texEnv(8960, GLX.GL_COMBINE_ALPHA, 7681);
            GlStateManager.texEnv(8960, GLX.GL_SOURCE0_ALPHA, GLX.GL_PREVIOUS);
            GlStateManager.texEnv(8960, GLX.GL_OPERAND0_ALPHA, 770);
            GlStateManager.activeTexture(GLX.GL_TEXTURE0);
            return true;
        }
    }

    protected void teardownOverlayColor() {
        GlStateManager.activeTexture(GLX.GL_TEXTURE0);
        GlStateManager.enableTexture();
        GlStateManager.texEnv(8960, 8704, GLX.GL_COMBINE);
        GlStateManager.texEnv(8960, GLX.GL_COMBINE_RGB, 8448);
        GlStateManager.texEnv(8960, GLX.GL_SOURCE0_RGB, GLX.GL_TEXTURE0);
        GlStateManager.texEnv(8960, GLX.GL_SOURCE1_RGB, GLX.GL_PRIMARY_COLOR);
        GlStateManager.texEnv(8960, GLX.GL_OPERAND0_RGB, 768);
        GlStateManager.texEnv(8960, GLX.GL_OPERAND1_RGB, 768);
        GlStateManager.texEnv(8960, GLX.GL_COMBINE_ALPHA, 8448);
        GlStateManager.texEnv(8960, GLX.GL_SOURCE0_ALPHA, GLX.GL_TEXTURE0);
        GlStateManager.texEnv(8960, GLX.GL_SOURCE1_ALPHA, GLX.GL_PRIMARY_COLOR);
        GlStateManager.texEnv(8960, GLX.GL_OPERAND0_ALPHA, 770);
        GlStateManager.texEnv(8960, GLX.GL_OPERAND1_ALPHA, 770);
        GlStateManager.activeTexture(GLX.GL_TEXTURE1);
        GlStateManager.texEnv(8960, 8704, GLX.GL_COMBINE);
        GlStateManager.texEnv(8960, GLX.GL_COMBINE_RGB, 8448);
        GlStateManager.texEnv(8960, GLX.GL_OPERAND0_RGB, 768);
        GlStateManager.texEnv(8960, GLX.GL_OPERAND1_RGB, 768);
        GlStateManager.texEnv(8960, GLX.GL_SOURCE0_RGB, 5890);
        GlStateManager.texEnv(8960, GLX.GL_SOURCE1_RGB, GLX.GL_PREVIOUS);
        GlStateManager.texEnv(8960, GLX.GL_COMBINE_ALPHA, 8448);
        GlStateManager.texEnv(8960, GLX.GL_OPERAND0_ALPHA, 770);
        GlStateManager.texEnv(8960, GLX.GL_SOURCE0_ALPHA, 5890);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.activeTexture(GLX.GL_TEXTURE2);
        GlStateManager.disableTexture();
        GlStateManager.bindTexture(0);
        GlStateManager.texEnv(8960, 8704, GLX.GL_COMBINE);
        GlStateManager.texEnv(8960, GLX.GL_COMBINE_RGB, 8448);
        GlStateManager.texEnv(8960, GLX.GL_OPERAND0_RGB, 768);
        GlStateManager.texEnv(8960, GLX.GL_OPERAND1_RGB, 768);
        GlStateManager.texEnv(8960, GLX.GL_SOURCE0_RGB, 5890);
        GlStateManager.texEnv(8960, GLX.GL_SOURCE1_RGB, GLX.GL_PREVIOUS);
        GlStateManager.texEnv(8960, GLX.GL_COMBINE_ALPHA, 8448);
        GlStateManager.texEnv(8960, GLX.GL_OPERAND0_ALPHA, 770);
        GlStateManager.texEnv(8960, GLX.GL_SOURCE0_ALPHA, 5890);
        GlStateManager.activeTexture(GLX.GL_TEXTURE0);
    }

    protected void setupPosition(T param0, double param1, double param2, double param3) {
        if (param0.getPose() == Pose.SLEEPING) {
            Direction var0 = param0.getBedOrientation();
            if (var0 != null) {
                float var1 = param0.getEyeHeight(Pose.STANDING) - 0.1F;
                GlStateManager.translatef((float)param1 - (float)var0.getStepX() * var1, (float)param2, (float)param3 - (float)var0.getStepZ() * var1);
                return;
            }
        }

        GlStateManager.translatef((float)param1, (float)param2, (float)param3);
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

    protected void setupRotations(T param0, float param1, float param2, float param3) {
        Pose var0 = param0.getPose();
        if (var0 != Pose.SLEEPING) {
            GlStateManager.rotatef(180.0F - param2, 0.0F, 1.0F, 0.0F);
        }

        if (param0.deathTime > 0) {
            float var1 = ((float)param0.deathTime + param3 - 1.0F) / 20.0F * 1.6F;
            var1 = Mth.sqrt(var1);
            if (var1 > 1.0F) {
                var1 = 1.0F;
            }

            GlStateManager.rotatef(var1 * this.getFlipDegrees(param0), 0.0F, 0.0F, 1.0F);
        } else if (param0.isAutoSpinAttack()) {
            GlStateManager.rotatef(-90.0F - param0.xRot, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotatef(((float)param0.tickCount + param3) * -75.0F, 0.0F, 1.0F, 0.0F);
        } else if (var0 == Pose.SLEEPING) {
            Direction var2 = param0.getBedOrientation();
            GlStateManager.rotatef(var2 != null ? sleepDirectionToRotation(var2) : param2, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotatef(this.getFlipDegrees(param0), 0.0F, 0.0F, 1.0F);
            GlStateManager.rotatef(270.0F, 0.0F, 1.0F, 0.0F);
        } else if (param0.hasCustomName() || param0 instanceof Player) {
            String var3 = ChatFormatting.stripFormatting(param0.getName().getString());
            if (var3 != null
                && ("Dinnerbone".equals(var3) || "Grumm".equals(var3))
                && (!(param0 instanceof Player) || ((Player)param0).isModelPartShown(PlayerModelPart.CAPE))) {
                GlStateManager.translatef(0.0F, param0.getBbHeight() + 0.1F, 0.0F);
                GlStateManager.rotatef(180.0F, 0.0F, 0.0F, 1.0F);
            }
        }

    }

    protected float getAttackAnim(T param0, float param1) {
        return param0.getAttackAnim(param1);
    }

    protected float getBob(T param0, float param1) {
        return (float)param0.tickCount + param1;
    }

    protected void renderLayers(T param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7) {
        for(RenderLayer<T, M> var0 : this.layers) {
            boolean var1 = this.setupOverlayColor(param0, param3, var0.colorsOnDamage());
            var0.render(param0, param1, param2, param3, param4, param5, param6, param7);
            if (var1) {
                this.teardownOverlayColor();
            }
        }

    }

    protected float getFlipDegrees(T param0) {
        return 90.0F;
    }

    protected int getOverlayColor(T param0, float param1, float param2) {
        return 0;
    }

    protected void scale(T param0, float param1) {
    }

    public void renderName(T param0, double param1, double param2, double param3) {
        if (this.shouldShowName(param0)) {
            double var0 = param0.distanceToSqr(this.entityRenderDispatcher.camera.getPosition());
            float var1 = param0.isVisuallySneaking() ? 32.0F : 64.0F;
            if (!(var0 >= (double)(var1 * var1))) {
                String var2 = param0.getDisplayName().getColoredString();
                GlStateManager.alphaFunc(516, 0.1F);
                this.renderNameTags(param0, param1, param2, param3, var2, var0);
            }
        }
    }

    protected boolean shouldShowName(T param0) {
        LocalPlayer var0 = Minecraft.getInstance().player;
        boolean var1 = !param0.isInvisibleTo(var0);
        if (param0 != var0) {
            Team var2 = param0.getTeam();
            Team var3 = var0.getTeam();
            if (var2 != null) {
                Team.Visibility var4 = var2.getNameTagVisibility();
                switch(var4) {
                    case ALWAYS:
                        return var1;
                    case NEVER:
                        return false;
                    case HIDE_FOR_OTHER_TEAMS:
                        return var3 == null ? var1 : var2.isAlliedTo(var3) && (var2.canSeeFriendlyInvisibles() || var1);
                    case HIDE_FOR_OWN_TEAM:
                        return var3 == null ? var1 : !var2.isAlliedTo(var3) && var1;
                    default:
                        return true;
                }
            }
        }

        return Minecraft.renderNames() && param0 != this.entityRenderDispatcher.camera.getEntity() && var1 && !param0.isVehicle();
    }
}

package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class EntityRenderer<T extends Entity> {
    protected final EntityRenderDispatcher entityRenderDispatcher;
    protected float shadowRadius;
    protected float shadowStrength = 1.0F;

    protected EntityRenderer(EntityRenderDispatcher param0) {
        this.entityRenderDispatcher = param0;
    }

    public final int getPackedLightCoords(T param0, float param1) {
        BlockPos var0 = new BlockPos(param0.getLightProbePosition(param1));
        return LightTexture.pack(this.getBlockLightLevel(param0, var0), this.getSkyLightLevel(param0, var0));
    }

    protected int getSkyLightLevel(T param0, BlockPos param1) {
        return param0.level.getBrightness(LightLayer.SKY, param1);
    }

    protected int getBlockLightLevel(T param0, BlockPos param1) {
        return param0.isOnFire() ? 15 : param0.level.getBrightness(LightLayer.BLOCK, param1);
    }

    public boolean shouldRender(T param0, Frustum param1, double param2, double param3, double param4) {
        if (!param0.shouldRender(param2, param3, param4)) {
            return false;
        } else if (param0.noCulling) {
            return true;
        } else {
            AABB var0 = param0.getBoundingBoxForCulling().inflate(0.5);
            if (var0.hasNaN() || var0.getSize() == 0.0) {
                var0 = new AABB(param0.getX() - 2.0, param0.getY() - 2.0, param0.getZ() - 2.0, param0.getX() + 2.0, param0.getY() + 2.0, param0.getZ() + 2.0);
            }

            return param1.isVisible(var0);
        }
    }

    public Vec3 getRenderOffset(T param0, float param1) {
        return Vec3.ZERO;
    }

    public void render(T param0, float param1, float param2, PoseStack param3, MultiBufferSource param4, int param5) {
        if (this.shouldShowName(param0)) {
            this.renderNameTag(param0, param0.getDisplayName(), param3, param4, param5);
        }
    }

    protected boolean shouldShowName(T param0) {
        return param0.shouldShowName() && param0.hasCustomName();
    }

    public abstract ResourceLocation getTextureLocation(T var1);

    public Font getFont() {
        return this.entityRenderDispatcher.getFont();
    }

    protected void renderNameTag(T param0, Component param1, PoseStack param2, MultiBufferSource param3, int param4) {
        double var0 = this.entityRenderDispatcher.distanceToSqr(param0);
        if (!(var0 > 4096.0)) {
            boolean var1 = !param0.isDiscrete();
            float var2 = param0.getBbHeight() + 0.5F;
            int var3 = "deadmau5".equals(param1.getString()) ? -10 : 0;
            param2.pushPose();
            param2.translate(0.0, (double)var2, 0.0);
            param2.mulPose(this.entityRenderDispatcher.cameraOrientation());
            param2.scale(-0.025F, -0.025F, 0.025F);
            Matrix4f var4 = param2.last().pose();
            float var5 = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
            int var6 = (int)(var5 * 255.0F) << 24;
            Font var7 = this.getFont();
            float var8 = (float)(-var7.width(param1) / 2);
            var7.drawInBatch(param1, var8, (float)var3, 553648127, false, var4, param3, var1, var6, param4);
            if (var1) {
                var7.drawInBatch(param1, var8, (float)var3, -1, false, var4, param3, false, 0, param4);
            }

            param2.popPose();
        }
    }

    public EntityRenderDispatcher getDispatcher() {
        return this.entityRenderDispatcher;
    }
}

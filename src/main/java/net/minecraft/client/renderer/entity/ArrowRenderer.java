package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class ArrowRenderer<T extends AbstractArrow> extends EntityRenderer<T> {
    public ArrowRenderer(EntityRenderDispatcher param0) {
        super(param0);
    }

    public void render(T param0, double param1, double param2, double param3, float param4, float param5, PoseStack param6, MultiBufferSource param7) {
        param6.pushPose();
        param6.mulPose(Vector3f.YP.rotationDegrees(Mth.lerp(param5, param0.yRotO, param0.yRot) - 90.0F));
        param6.mulPose(Vector3f.ZP.rotationDegrees(Mth.lerp(param5, param0.xRotO, param0.xRot)));
        int var0 = 0;
        float var1 = 0.0F;
        float var2 = 0.5F;
        float var3 = 0.0F;
        float var4 = 0.15625F;
        float var5 = 0.0F;
        float var6 = 0.15625F;
        float var7 = 0.15625F;
        float var8 = 0.3125F;
        float var9 = 0.05625F;
        float var10 = (float)param0.shakeTime - param5;
        if (var10 > 0.0F) {
            float var11 = -Mth.sin(var10 * 3.0F) * var10;
            param6.mulPose(Vector3f.ZP.rotationDegrees(var11));
        }

        param6.mulPose(Vector3f.XP.rotationDegrees(45.0F));
        param6.scale(0.05625F, 0.05625F, 0.05625F);
        param6.translate(-4.0, 0.0, 0.0);
        int var12 = param0.getLightColor();
        VertexConsumer var13 = param7.getBuffer(RenderType.entityCutoutNoCull(this.getTextureLocation(param0)));
        Matrix4f var14 = param6.getPose();
        this.vertex(var14, var13, -7, -2, -2, 0.0F, 0.15625F, 1, 0, 0, var12);
        this.vertex(var14, var13, -7, -2, 2, 0.15625F, 0.15625F, 1, 0, 0, var12);
        this.vertex(var14, var13, -7, 2, 2, 0.15625F, 0.3125F, 1, 0, 0, var12);
        this.vertex(var14, var13, -7, 2, -2, 0.0F, 0.3125F, 1, 0, 0, var12);
        this.vertex(var14, var13, -7, 2, -2, 0.0F, 0.15625F, -1, 0, 0, var12);
        this.vertex(var14, var13, -7, 2, 2, 0.15625F, 0.15625F, -1, 0, 0, var12);
        this.vertex(var14, var13, -7, -2, 2, 0.15625F, 0.3125F, -1, 0, 0, var12);
        this.vertex(var14, var13, -7, -2, -2, 0.0F, 0.3125F, -1, 0, 0, var12);

        for(int var15 = 0; var15 < 4; ++var15) {
            param6.mulPose(Vector3f.XP.rotationDegrees(90.0F));
            this.vertex(var14, var13, -8, -2, 0, 0.0F, 0.0F, 0, 1, 0, var12);
            this.vertex(var14, var13, 8, -2, 0, 0.5F, 0.0F, 0, 1, 0, var12);
            this.vertex(var14, var13, 8, 2, 0, 0.5F, 0.15625F, 0, 1, 0, var12);
            this.vertex(var14, var13, -8, 2, 0, 0.0F, 0.15625F, 0, 1, 0, var12);
        }

        param6.popPose();
        super.render(param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public void vertex(
        Matrix4f param0, VertexConsumer param1, int param2, int param3, int param4, float param5, float param6, int param7, int param8, int param9, int param10
    ) {
        param1.vertex(param0, (float)param2, (float)param3, (float)param4)
            .color(255, 255, 255, 255)
            .uv(param5, param6)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(param10)
            .normal((float)param7, (float)param9, (float)param8)
            .endVertex();
    }
}

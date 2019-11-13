package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
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

    public void render(T param0, float param1, float param2, PoseStack param3, MultiBufferSource param4, int param5) {
        param3.pushPose();
        param3.mulPose(Vector3f.YP.rotationDegrees(Mth.lerp(param2, param0.yRotO, param0.yRot) - 90.0F));
        param3.mulPose(Vector3f.ZP.rotationDegrees(Mth.lerp(param2, param0.xRotO, param0.xRot)));
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
        float var10 = (float)param0.shakeTime - param2;
        if (var10 > 0.0F) {
            float var11 = -Mth.sin(var10 * 3.0F) * var10;
            param3.mulPose(Vector3f.ZP.rotationDegrees(var11));
        }

        param3.mulPose(Vector3f.XP.rotationDegrees(45.0F));
        param3.scale(0.05625F, 0.05625F, 0.05625F);
        param3.translate(-4.0, 0.0, 0.0);
        VertexConsumer var12 = param4.getBuffer(RenderType.entityCutout(this.getTextureLocation(param0)));
        PoseStack.Pose var13 = param3.last();
        Matrix4f var14 = var13.pose();
        Matrix3f var15 = var13.normal();
        this.vertex(var14, var15, var12, -7, -2, -2, 0.0F, 0.15625F, 1, 0, 0, param5);
        this.vertex(var14, var15, var12, -7, -2, 2, 0.15625F, 0.15625F, 1, 0, 0, param5);
        this.vertex(var14, var15, var12, -7, 2, 2, 0.15625F, 0.3125F, 1, 0, 0, param5);
        this.vertex(var14, var15, var12, -7, 2, -2, 0.0F, 0.3125F, 1, 0, 0, param5);
        this.vertex(var14, var15, var12, -7, 2, -2, 0.0F, 0.15625F, -1, 0, 0, param5);
        this.vertex(var14, var15, var12, -7, 2, 2, 0.15625F, 0.15625F, -1, 0, 0, param5);
        this.vertex(var14, var15, var12, -7, -2, 2, 0.15625F, 0.3125F, -1, 0, 0, param5);
        this.vertex(var14, var15, var12, -7, -2, -2, 0.0F, 0.3125F, -1, 0, 0, param5);

        for(int var16 = 0; var16 < 4; ++var16) {
            param3.mulPose(Vector3f.XP.rotationDegrees(90.0F));
            this.vertex(var14, var15, var12, -8, -2, 0, 0.0F, 0.0F, 0, 1, 0, param5);
            this.vertex(var14, var15, var12, 8, -2, 0, 0.5F, 0.0F, 0, 1, 0, param5);
            this.vertex(var14, var15, var12, 8, 2, 0, 0.5F, 0.15625F, 0, 1, 0, param5);
            this.vertex(var14, var15, var12, -8, 2, 0, 0.0F, 0.15625F, 0, 1, 0, param5);
        }

        param3.popPose();
        super.render(param0, param1, param2, param3, param4, param5);
    }

    public void vertex(
        Matrix4f param0,
        Matrix3f param1,
        VertexConsumer param2,
        int param3,
        int param4,
        int param5,
        float param6,
        float param7,
        int param8,
        int param9,
        int param10,
        int param11
    ) {
        param2.vertex(param0, (float)param3, (float)param4, (float)param5)
            .color(255, 255, 255, 255)
            .uv(param6, param7)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(param11)
            .normal(param1, (float)param8, (float)param10, (float)param9)
            .endVertex();
    }
}

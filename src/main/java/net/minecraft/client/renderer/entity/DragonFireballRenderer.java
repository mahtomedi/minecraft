package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DragonFireballRenderer extends EntityRenderer<DragonFireball> {
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/enderdragon/dragon_fireball.png");

    public DragonFireballRenderer(EntityRenderDispatcher param0) {
        super(param0);
    }

    public void render(DragonFireball param0, float param1, float param2, PoseStack param3, MultiBufferSource param4, int param5) {
        param3.pushPose();
        param3.scale(2.0F, 2.0F, 2.0F);
        param3.mulPose(this.entityRenderDispatcher.camera.rotation());
        param3.mulPose(Vector3f.YP.rotationDegrees(180.0F));
        PoseStack.Pose var0 = param3.last();
        Matrix4f var1 = var0.pose();
        Matrix3f var2 = var0.normal();
        VertexConsumer var3 = param4.getBuffer(RenderType.entityCutoutNoCull(TEXTURE_LOCATION));
        vertex(var3, var1, var2, param5, 0.0F, 0, 0, 1);
        vertex(var3, var1, var2, param5, 1.0F, 0, 1, 1);
        vertex(var3, var1, var2, param5, 1.0F, 1, 1, 0);
        vertex(var3, var1, var2, param5, 0.0F, 1, 0, 0);
        param3.popPose();
        super.render(param0, param1, param2, param3, param4, param5);
    }

    private static void vertex(VertexConsumer param0, Matrix4f param1, Matrix3f param2, int param3, float param4, int param5, int param6, int param7) {
        param0.vertex(param1, param4 - 0.5F, (float)param5 - 0.25F, 0.0F)
            .color(255, 255, 255, 255)
            .uv((float)param6, (float)param7)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(param3)
            .normal(param2, 0.0F, 1.0F, 0.0F)
            .endVertex();
    }

    public ResourceLocation getTextureLocation(DragonFireball param0) {
        return TEXTURE_LOCATION;
    }
}

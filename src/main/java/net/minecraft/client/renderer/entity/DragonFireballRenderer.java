package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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

    public void render(
        DragonFireball param0, double param1, double param2, double param3, float param4, float param5, PoseStack param6, MultiBufferSource param7
    ) {
        param6.pushPose();
        param6.scale(2.0F, 2.0F, 2.0F);
        float var0 = 1.0F;
        float var1 = 0.5F;
        float var2 = 0.25F;
        param6.mulPose(Vector3f.YP.rotationDegrees(180.0F - this.entityRenderDispatcher.playerRotY));
        float var3 = (float)(this.entityRenderDispatcher.options.thirdPersonView == 2 ? -1 : 1) * -this.entityRenderDispatcher.playerRotX;
        param6.mulPose(Vector3f.XP.rotationDegrees(var3));
        Matrix4f var4 = param6.getPose();
        VertexConsumer var5 = param7.getBuffer(RenderType.entityCutoutNoCull(TEXTURE_LOCATION));
        int var6 = param0.getLightColor();
        var5.vertex(var4, -0.5F, -0.25F, 0.0F)
            .color(255, 255, 255, 255)
            .uv(0.0F, 1.0F)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(var6)
            .normal(0.0F, 1.0F, 0.0F)
            .endVertex();
        var5.vertex(var4, 0.5F, -0.25F, 0.0F)
            .color(255, 255, 255, 255)
            .uv(1.0F, 1.0F)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(var6)
            .normal(0.0F, 1.0F, 0.0F)
            .endVertex();
        var5.vertex(var4, 0.5F, 0.75F, 0.0F)
            .color(255, 255, 255, 255)
            .uv(1.0F, 0.0F)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(var6)
            .normal(0.0F, 1.0F, 0.0F)
            .endVertex();
        var5.vertex(var4, -0.5F, 0.75F, 0.0F)
            .color(255, 255, 255, 255)
            .uv(0.0F, 0.0F)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(var6)
            .normal(0.0F, 1.0F, 0.0F)
            .endVertex();
        param6.popPose();
        super.render(param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public ResourceLocation getTextureLocation(DragonFireball param0) {
        return TEXTURE_LOCATION;
    }
}

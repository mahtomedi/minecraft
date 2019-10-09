package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.EvokerFangsModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EvokerFangsRenderer extends EntityRenderer<EvokerFangs> {
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/illager/evoker_fangs.png");
    private final EvokerFangsModel<EvokerFangs> model = new EvokerFangsModel<>();

    public EvokerFangsRenderer(EntityRenderDispatcher param0) {
        super(param0);
    }

    public void render(EvokerFangs param0, double param1, double param2, double param3, float param4, float param5, PoseStack param6, MultiBufferSource param7) {
        float var0 = param0.getAnimationProgress(param5);
        if (var0 != 0.0F) {
            float var1 = 2.0F;
            if (var0 > 0.9F) {
                var1 = (float)((double)var1 * ((1.0 - (double)var0) / 0.1F));
            }

            param6.pushPose();
            param6.mulPose(Vector3f.YP.rotationDegrees(90.0F - param0.yRot));
            param6.scale(-var1, -var1, var1);
            float var2 = 0.03125F;
            param6.translate(0.0, -0.626F, 0.0);
            int var3 = param0.getLightColor();
            this.model.setupAnim(param0, var0, 0.0F, 0.0F, param0.yRot, param0.xRot, 0.03125F);
            VertexConsumer var4 = param7.getBuffer(this.model.renderType(TEXTURE_LOCATION));
            this.model.renderToBuffer(param6, var4, var3, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F);
            param6.popPose();
            super.render(param0, param1, param2, param3, param4, param5, param6, param7);
        }
    }

    public ResourceLocation getTextureLocation(EvokerFangs param0) {
        return TEXTURE_LOCATION;
    }
}

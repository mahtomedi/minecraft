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

    public void render(EvokerFangs param0, float param1, float param2, PoseStack param3, MultiBufferSource param4, int param5) {
        float var0 = param0.getAnimationProgress(param2);
        if (var0 != 0.0F) {
            float var1 = 2.0F;
            if (var0 > 0.9F) {
                var1 = (float)((double)var1 * ((1.0 - (double)var0) / 0.1F));
            }

            param3.pushPose();
            param3.mulPose(Vector3f.YP.rotationDegrees(90.0F - param0.yRot));
            param3.scale(-var1, -var1, var1);
            float var2 = 0.03125F;
            param3.translate(0.0, -0.626F, 0.0);
            param3.scale(0.5F, 0.5F, 0.5F);
            this.model.setupAnim(param0, var0, 0.0F, 0.0F, param0.yRot, param0.xRot);
            VertexConsumer var3 = param4.getBuffer(this.model.renderType(TEXTURE_LOCATION));
            this.model.renderToBuffer(param3, var3, param5, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F);
            param3.popPose();
            super.render(param0, param1, param2, param3, param4, param5);
        }
    }

    public ResourceLocation getTextureLocation(EvokerFangs param0) {
        return TEXTURE_LOCATION;
    }
}

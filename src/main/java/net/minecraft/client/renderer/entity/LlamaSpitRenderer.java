package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.LlamaSpitModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LlamaSpitRenderer extends EntityRenderer<LlamaSpit> {
    private static final ResourceLocation LLAMA_SPIT_LOCATION = new ResourceLocation("textures/entity/llama/spit.png");
    private final LlamaSpitModel<LlamaSpit> model;

    public LlamaSpitRenderer(EntityRendererProvider.Context param0) {
        super(param0);
        this.model = new LlamaSpitModel<>(param0.bakeLayer(ModelLayers.LLAMA_SPIT));
    }

    public void render(LlamaSpit param0, float param1, float param2, PoseStack param3, MultiBufferSource param4, int param5) {
        param3.pushPose();
        param3.translate(0.0F, 0.15F, 0.0F);
        param3.mulPose(Axis.YP.rotationDegrees(Mth.lerp(param2, param0.yRotO, param0.getYRot()) - 90.0F));
        param3.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(param2, param0.xRotO, param0.getXRot())));
        this.model.setupAnim(param0, param2, 0.0F, -0.1F, 0.0F, 0.0F);
        VertexConsumer var0 = param4.getBuffer(this.model.renderType(LLAMA_SPIT_LOCATION));
        this.model.renderToBuffer(param3, var0, param5, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        param3.popPose();
        super.render(param0, param1, param2, param3, param4, param5);
    }

    public ResourceLocation getTextureLocation(LlamaSpit param0) {
        return LLAMA_SPIT_LOCATION;
    }
}

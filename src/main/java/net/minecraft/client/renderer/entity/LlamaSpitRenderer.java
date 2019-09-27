package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.LlamaSpitModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LlamaSpitRenderer extends EntityRenderer<LlamaSpit> {
    private static final ResourceLocation LLAMA_SPIT_LOCATION = new ResourceLocation("textures/entity/llama/spit.png");
    private final LlamaSpitModel<LlamaSpit> model = new LlamaSpitModel<>();

    public LlamaSpitRenderer(EntityRenderDispatcher param0) {
        super(param0);
    }

    public void render(LlamaSpit param0, double param1, double param2, double param3, float param4, float param5, PoseStack param6, MultiBufferSource param7) {
        param6.pushPose();
        param6.translate(0.0, 0.15F, 0.0);
        param6.mulPose(Vector3f.YP.rotation(Mth.lerp(param5, param0.yRotO, param0.yRot) - 90.0F, true));
        param6.mulPose(Vector3f.ZP.rotation(Mth.lerp(param5, param0.xRotO, param0.xRot), true));
        int var0 = param0.getLightColor();
        VertexConsumer var1 = param7.getBuffer(RenderType.NEW_ENTITY(LLAMA_SPIT_LOCATION));
        OverlayTexture.setDefault(var1);
        this.model.setupAnim(param0, param5, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
        this.model.renderToBuffer(param6, var1, var0);
        var1.unsetDefaultOverlayCoords();
        param6.popPose();
        super.render(param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public ResourceLocation getTextureLocation(LlamaSpit param0) {
        return LLAMA_SPIT_LOCATION;
    }
}

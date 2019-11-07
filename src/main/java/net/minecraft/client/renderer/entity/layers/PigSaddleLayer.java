package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PigModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Pig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PigSaddleLayer extends RenderLayer<Pig, PigModel<Pig>> {
    private static final ResourceLocation SADDLE_LOCATION = new ResourceLocation("textures/entity/pig/pig_saddle.png");
    private final PigModel<Pig> model = new PigModel<>(0.5F);

    public PigSaddleLayer(RenderLayerParent<Pig, PigModel<Pig>> param0) {
        super(param0);
    }

    public void render(
        PoseStack param0, MultiBufferSource param1, int param2, Pig param3, float param4, float param5, float param6, float param7, float param8, float param9
    ) {
        if (param3.hasSaddle()) {
            this.getParentModel().copyPropertiesTo(this.model);
            this.model.prepareMobModel(param3, param4, param5, param6);
            this.model.setupAnim(param3, param4, param5, param7, param8, param9);
            VertexConsumer var0 = param1.getBuffer(RenderType.entityCutoutNoCull(SADDLE_LOCATION));
            this.model.renderToBuffer(param0, var0, param2, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F);
        }
    }
}

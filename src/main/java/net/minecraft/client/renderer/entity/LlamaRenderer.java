package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.LlamaModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.layers.LlamaDecorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LlamaRenderer extends MobRenderer<Llama, LlamaModel<Llama>> {
    private static final ResourceLocation CREAMY = new ResourceLocation("textures/entity/llama/creamy.png");
    private static final ResourceLocation WHITE = new ResourceLocation("textures/entity/llama/white.png");
    private static final ResourceLocation BROWN = new ResourceLocation("textures/entity/llama/brown.png");
    private static final ResourceLocation GRAY = new ResourceLocation("textures/entity/llama/gray.png");

    public LlamaRenderer(EntityRendererProvider.Context param0, ModelLayerLocation param1) {
        super(param0, new LlamaModel<>(param0.bakeLayer(param1)), 0.7F);
        this.addLayer(new LlamaDecorLayer(this, param0.getModelSet()));
    }

    public ResourceLocation getTextureLocation(Llama param0) {
        return switch(param0.getVariant()) {
            case CREAMY -> CREAMY;
            case WHITE -> WHITE;
            case BROWN -> BROWN;
            case GRAY -> GRAY;
        };
    }
}

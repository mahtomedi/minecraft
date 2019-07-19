package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.model.LlamaModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LlamaDecorLayer extends RenderLayer<Llama, LlamaModel<Llama>> {
    private static final ResourceLocation[] TEXTURE_LOCATION = new ResourceLocation[]{
        new ResourceLocation("textures/entity/llama/decor/white.png"),
        new ResourceLocation("textures/entity/llama/decor/orange.png"),
        new ResourceLocation("textures/entity/llama/decor/magenta.png"),
        new ResourceLocation("textures/entity/llama/decor/light_blue.png"),
        new ResourceLocation("textures/entity/llama/decor/yellow.png"),
        new ResourceLocation("textures/entity/llama/decor/lime.png"),
        new ResourceLocation("textures/entity/llama/decor/pink.png"),
        new ResourceLocation("textures/entity/llama/decor/gray.png"),
        new ResourceLocation("textures/entity/llama/decor/light_gray.png"),
        new ResourceLocation("textures/entity/llama/decor/cyan.png"),
        new ResourceLocation("textures/entity/llama/decor/purple.png"),
        new ResourceLocation("textures/entity/llama/decor/blue.png"),
        new ResourceLocation("textures/entity/llama/decor/brown.png"),
        new ResourceLocation("textures/entity/llama/decor/green.png"),
        new ResourceLocation("textures/entity/llama/decor/red.png"),
        new ResourceLocation("textures/entity/llama/decor/black.png")
    };
    private static final ResourceLocation TRADER_LLAMA = new ResourceLocation("textures/entity/llama/decor/trader_llama.png");
    private final LlamaModel<Llama> model = new LlamaModel<>(0.5F);

    public LlamaDecorLayer(RenderLayerParent<Llama, LlamaModel<Llama>> param0) {
        super(param0);
    }

    public void render(Llama param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7) {
        DyeColor var0 = param0.getSwag();
        if (var0 != null) {
            this.bindTexture(TEXTURE_LOCATION[var0.getId()]);
        } else {
            if (!param0.isTraderLlama()) {
                return;
            }

            this.bindTexture(TRADER_LLAMA);
        }

        this.getParentModel().copyPropertiesTo(this.model);
        this.model.render(param0, param1, param2, param4, param5, param6, param7);
    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }
}

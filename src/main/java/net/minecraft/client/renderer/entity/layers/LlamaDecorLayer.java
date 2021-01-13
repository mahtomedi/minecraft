package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.LlamaModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
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

    public void render(
        PoseStack param0,
        MultiBufferSource param1,
        int param2,
        Llama param3,
        float param4,
        float param5,
        float param6,
        float param7,
        float param8,
        float param9
    ) {
        DyeColor var0 = param3.getSwag();
        ResourceLocation var1;
        if (var0 != null) {
            var1 = TEXTURE_LOCATION[var0.getId()];
        } else {
            if (!param3.isTraderLlama()) {
                return;
            }

            var1 = TRADER_LLAMA;
        }

        this.getParentModel().copyPropertiesTo(this.model);
        this.model.setupAnim(param3, param4, param5, param7, param8, param9);
        VertexConsumer var4 = param1.getBuffer(RenderType.entityCutoutNoCull(var1));
        this.model.renderToBuffer(param0, var4, param2, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }
}

package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WardenEmissiveLayer<T extends Warden, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private final ResourceLocation texture;
    private final WardenEmissiveLayer.AlphaFunction<T> alphaFunction;

    public WardenEmissiveLayer(RenderLayerParent<T, M> param0, ResourceLocation param1, WardenEmissiveLayer.AlphaFunction<T> param2) {
        super(param0);
        this.texture = param1;
        this.alphaFunction = param2;
    }

    public void render(
        PoseStack param0, MultiBufferSource param1, int param2, T param3, float param4, float param5, float param6, float param7, float param8, float param9
    ) {
        if (!param3.isInvisible()) {
            VertexConsumer var0 = param1.getBuffer(RenderType.entityTranslucentEmissive(this.texture));
            this.getParentModel()
                .renderToBuffer(
                    param0,
                    var0,
                    param2,
                    LivingEntityRenderer.getOverlayCoords(param3, 0.0F),
                    1.0F,
                    1.0F,
                    1.0F,
                    this.alphaFunction.apply(param3, param6, param7)
                );
        }
    }

    @OnlyIn(Dist.CLIENT)
    public interface AlphaFunction<T extends Warden> {
        float apply(T var1, float var2, float var3);
    }
}

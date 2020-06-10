package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.DrownedModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DrownedOuterLayer<T extends Drowned> extends RenderLayer<T, DrownedModel<T>> {
    private static final ResourceLocation DROWNED_OUTER_LAYER_LOCATION = new ResourceLocation("textures/entity/zombie/drowned_outer_layer.png");
    private final DrownedModel<T> model = new DrownedModel<>(0.25F, 0.0F, 64, 64);

    public DrownedOuterLayer(RenderLayerParent<T, DrownedModel<T>> param0) {
        super(param0);
    }

    public void render(
        PoseStack param0, MultiBufferSource param1, int param2, T param3, float param4, float param5, float param6, float param7, float param8, float param9
    ) {
        coloredCutoutModelCopyLayerRender(
            this.getParentModel(),
            this.model,
            DROWNED_OUTER_LAYER_LOCATION,
            param0,
            param1,
            param2,
            param3,
            param4,
            param5,
            param7,
            param8,
            param9,
            param6,
            1.0F,
            1.0F,
            1.0F
        );
    }
}

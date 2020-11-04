package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ColorableHierarchicalModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.TropicalFishModelA;
import net.minecraft.client.model.TropicalFishModelB;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TropicalFishPatternLayer extends RenderLayer<TropicalFish, ColorableHierarchicalModel<TropicalFish>> {
    private final TropicalFishModelA<TropicalFish> modelA;
    private final TropicalFishModelB<TropicalFish> modelB;

    public TropicalFishPatternLayer(RenderLayerParent<TropicalFish, ColorableHierarchicalModel<TropicalFish>> param0, EntityModelSet param1) {
        super(param0);
        this.modelA = new TropicalFishModelA<>(param1.getLayer(ModelLayers.TROPICAL_FISH_SMALL_PATTERN));
        this.modelB = new TropicalFishModelB<>(param1.getLayer(ModelLayers.TROPICAL_FISH_LARGE_PATTERN));
    }

    public void render(
        PoseStack param0,
        MultiBufferSource param1,
        int param2,
        TropicalFish param3,
        float param4,
        float param5,
        float param6,
        float param7,
        float param8,
        float param9
    ) {
        EntityModel<TropicalFish> var0 = (EntityModel<TropicalFish>)(param3.getBaseVariant() == 0 ? this.modelA : this.modelB);
        float[] var1 = param3.getPatternColor();
        coloredCutoutModelCopyLayerRender(
            this.getParentModel(),
            var0,
            param3.getPatternTextureLocation(),
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
            var1[0],
            var1[1],
            var1[2]
        );
    }
}

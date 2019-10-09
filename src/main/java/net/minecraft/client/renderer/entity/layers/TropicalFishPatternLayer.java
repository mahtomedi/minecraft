package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.TropicalFishModelA;
import net.minecraft.client.model.TropicalFishModelB;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TropicalFishPatternLayer extends RenderLayer<TropicalFish, EntityModel<TropicalFish>> {
    private final TropicalFishModelA<TropicalFish> modelA = new TropicalFishModelA<>(0.008F);
    private final TropicalFishModelB<TropicalFish> modelB = new TropicalFishModelB<>(0.008F);

    public TropicalFishPatternLayer(RenderLayerParent<TropicalFish, EntityModel<TropicalFish>> param0) {
        super(param0);
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
        float param9,
        float param10
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
            param10,
            param6,
            var1[0],
            var1[1],
            var1[2]
        );
    }
}

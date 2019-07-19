package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.TropicalFishModelA;
import net.minecraft.client.model.TropicalFishModelB;
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

    public void render(TropicalFish param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7) {
        if (!param0.isInvisible()) {
            EntityModel<TropicalFish> var0 = (EntityModel<TropicalFish>)(param0.getBaseVariant() == 0 ? this.modelA : this.modelB);
            this.bindTexture(param0.getPatternTextureLocation());
            float[] var1 = param0.getPatternColor();
            GlStateManager.color3f(var1[0], var1[1], var1[2]);
            this.getParentModel().copyPropertiesTo(var0);
            var0.prepareMobModel(param0, param1, param2, param3);
            var0.render(param0, param1, param2, param4, param5, param6, param7);
        }
    }

    @Override
    public boolean colorsOnDamage() {
        return true;
    }
}

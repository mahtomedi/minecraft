package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.ColorableHierarchicalModel;
import net.minecraft.client.model.TropicalFishModelA;
import net.minecraft.client.model.TropicalFishModelB;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.TropicalFishPatternLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TropicalFishRenderer extends MobRenderer<TropicalFish, ColorableHierarchicalModel<TropicalFish>> {
    private final ColorableHierarchicalModel<TropicalFish> modelA = this.getModel();
    private final ColorableHierarchicalModel<TropicalFish> modelB;

    public TropicalFishRenderer(EntityRendererProvider.Context param0) {
        super(param0, new TropicalFishModelA<>(param0.getLayer(ModelLayers.TROPICAL_FISH_SMALL)), 0.15F);
        this.modelB = new TropicalFishModelB<>(param0.getLayer(ModelLayers.TROPICAL_FISH_LARGE));
        this.addLayer(new TropicalFishPatternLayer(this, param0.getModelSet()));
    }

    public ResourceLocation getTextureLocation(TropicalFish param0) {
        return param0.getBaseTextureLocation();
    }

    public void render(TropicalFish param0, float param1, float param2, PoseStack param3, MultiBufferSource param4, int param5) {
        ColorableHierarchicalModel<TropicalFish> var0 = param0.getBaseVariant() == 0 ? this.modelA : this.modelB;
        this.model = var0;
        float[] var1 = param0.getBaseColor();
        var0.setColor(var1[0], var1[1], var1[2]);
        super.render(param0, param1, param2, param3, param4, param5);
        var0.setColor(1.0F, 1.0F, 1.0F);
    }

    protected void setupRotations(TropicalFish param0, PoseStack param1, float param2, float param3, float param4) {
        super.setupRotations(param0, param1, param2, param3, param4);
        float var0 = 4.3F * Mth.sin(0.6F * param2);
        param1.mulPose(Vector3f.YP.rotationDegrees(var0));
        if (!param0.isInWater()) {
            param1.translate(0.2F, 0.1F, 0.0);
            param1.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
        }

    }
}

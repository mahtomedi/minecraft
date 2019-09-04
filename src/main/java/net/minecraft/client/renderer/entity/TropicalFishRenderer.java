package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.TropicalFishModelA;
import net.minecraft.client.model.TropicalFishModelB;
import net.minecraft.client.renderer.entity.layers.TropicalFishPatternLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TropicalFishRenderer extends MobRenderer<TropicalFish, EntityModel<TropicalFish>> {
    private final TropicalFishModelA<TropicalFish> modelA = new TropicalFishModelA<>();
    private final TropicalFishModelB<TropicalFish> modelB = new TropicalFishModelB<>();

    public TropicalFishRenderer(EntityRenderDispatcher param0) {
        super(param0, new TropicalFishModelA<>(), 0.15F);
        this.addLayer(new TropicalFishPatternLayer(this));
    }

    @Nullable
    protected ResourceLocation getTextureLocation(TropicalFish param0) {
        return param0.getBaseTextureLocation();
    }

    public void render(TropicalFish param0, double param1, double param2, double param3, float param4, float param5) {
        this.model = (EntityModel<TropicalFish>)(param0.getBaseVariant() == 0 ? this.modelA : this.modelB);
        float[] var0 = param0.getBaseColor();
        RenderSystem.color3f(var0[0], var0[1], var0[2]);
        super.render(param0, param1, param2, param3, param4, param5);
    }

    protected void setupRotations(TropicalFish param0, float param1, float param2, float param3) {
        super.setupRotations(param0, param1, param2, param3);
        float var0 = 4.3F * Mth.sin(0.6F * param1);
        RenderSystem.rotatef(var0, 0.0F, 1.0F, 0.0F);
        if (!param0.isInWater()) {
            RenderSystem.translatef(0.2F, 0.1F, 0.0F);
            RenderSystem.rotatef(90.0F, 0.0F, 0.0F, 1.0F);
        }

    }
}

package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.PigModel;
import net.minecraft.client.renderer.entity.layers.SaddleLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Pig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PigRenderer extends MobRenderer<Pig, PigModel<Pig>> {
    private static final ResourceLocation PIG_LOCATION = new ResourceLocation("textures/entity/pig/pig.png");

    public PigRenderer(EntityRenderDispatcher param0) {
        super(param0, new PigModel<>(), 0.7F);
        this.addLayer(new SaddleLayer<>(this, new PigModel<>(0.5F), new ResourceLocation("textures/entity/pig/pig_saddle.png")));
    }

    public ResourceLocation getTextureLocation(Pig param0) {
        return PIG_LOCATION;
    }
}

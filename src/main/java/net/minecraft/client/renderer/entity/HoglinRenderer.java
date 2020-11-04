package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.HoglinModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HoglinRenderer extends MobRenderer<Hoglin, HoglinModel<Hoglin>> {
    private static final ResourceLocation HOGLIN_LOCATION = new ResourceLocation("textures/entity/hoglin/hoglin.png");

    public HoglinRenderer(EntityRendererProvider.Context param0) {
        super(param0, new HoglinModel<>(param0.getLayer(ModelLayers.HOGLIN)), 0.7F);
    }

    public ResourceLocation getTextureLocation(Hoglin param0) {
        return HOGLIN_LOCATION;
    }

    protected boolean isShaking(Hoglin param0) {
        return param0.isConverting();
    }
}

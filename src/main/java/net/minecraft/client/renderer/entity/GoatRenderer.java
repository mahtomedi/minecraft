package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.GoatModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GoatRenderer extends MobRenderer<Goat, GoatModel<Goat>> {
    private static final ResourceLocation GOAT_LOCATION = new ResourceLocation("textures/entity/goat/goat.png");

    public GoatRenderer(EntityRendererProvider.Context param0) {
        super(param0, new GoatModel<>(param0.bakeLayer(ModelLayers.GOAT)), 0.7F);
        this.addLayer(new CustomHeadLayer<>(this, param0.getModelSet()));
    }

    public ResourceLocation getTextureLocation(Goat param0) {
        return GOAT_LOCATION;
    }
}

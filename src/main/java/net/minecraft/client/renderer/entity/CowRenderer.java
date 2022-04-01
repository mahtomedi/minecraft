package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.CowModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Cow;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CowRenderer extends MobRenderer<Cow, CowModel<Cow>> {
    private static final ResourceLocation COW_LOCATION = new ResourceLocation("textures/entity/cow/cow.png");

    public CowRenderer(EntityRendererProvider.Context param0) {
        super(param0, new CowModel<>(param0.bakeLayer(ModelLayers.COW)), 0.7F);
        this.addLayer(new CustomHeadLayer<>(this, param0.getModelSet()));
    }

    public ResourceLocation getTextureLocation(Cow param0) {
        return COW_LOCATION;
    }
}

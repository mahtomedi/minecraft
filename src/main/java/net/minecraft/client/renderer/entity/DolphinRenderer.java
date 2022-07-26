package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.DolphinModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.DolphinCarryingItemLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DolphinRenderer extends MobRenderer<Dolphin, DolphinModel<Dolphin>> {
    private static final ResourceLocation DOLPHIN_LOCATION = new ResourceLocation("textures/entity/dolphin.png");

    public DolphinRenderer(EntityRendererProvider.Context param0) {
        super(param0, new DolphinModel<>(param0.bakeLayer(ModelLayers.DOLPHIN)), 0.7F);
        this.addLayer(new DolphinCarryingItemLayer(this, param0.getItemInHandRenderer()));
    }

    public ResourceLocation getTextureLocation(Dolphin param0) {
        return DOLPHIN_LOCATION;
    }
}

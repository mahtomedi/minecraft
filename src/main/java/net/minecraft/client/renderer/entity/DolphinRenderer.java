package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.DolphinModel;
import net.minecraft.client.renderer.entity.layers.DolphinCarryingItemLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DolphinRenderer extends MobRenderer<Dolphin, DolphinModel<Dolphin>> {
    private static final ResourceLocation DOLPHIN_LOCATION = new ResourceLocation("textures/entity/dolphin.png");

    public DolphinRenderer(EntityRenderDispatcher param0) {
        super(param0, new DolphinModel<>(), 0.7F);
        this.addLayer(new DolphinCarryingItemLayer(this));
    }

    public ResourceLocation getTextureLocation(Dolphin param0) {
        return DOLPHIN_LOCATION;
    }
}

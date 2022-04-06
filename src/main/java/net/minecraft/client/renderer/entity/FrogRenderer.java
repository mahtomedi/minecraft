package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.FrogModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FrogRenderer extends MobRenderer<Frog, FrogModel<Frog>> {
    public FrogRenderer(EntityRendererProvider.Context param0) {
        super(param0, new FrogModel<>(param0.bakeLayer(ModelLayers.FROG)), 0.3F);
    }

    public ResourceLocation getTextureLocation(Frog param0) {
        return param0.getVariant().texture();
    }
}

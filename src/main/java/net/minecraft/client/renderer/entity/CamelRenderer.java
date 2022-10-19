package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.CamelModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CamelRenderer extends MobRenderer<Camel, CamelModel<Camel>> {
    private static final ResourceLocation CAMEL_LOCATION = new ResourceLocation("textures/entity/camel/camel.png");

    public CamelRenderer(EntityRendererProvider.Context param0, ModelLayerLocation param1) {
        super(param0, new CamelModel<>(param0.bakeLayer(param1)), 0.7F);
    }

    public ResourceLocation getTextureLocation(Camel param0) {
        return CAMEL_LOCATION;
    }
}

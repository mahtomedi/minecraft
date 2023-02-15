package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.SnifferModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SnifferRenderer extends MobRenderer<Sniffer, SnifferModel<Sniffer>> {
    private static final ResourceLocation SNIFFER_LOCATION = new ResourceLocation("textures/entity/sniffer/sniffer.png");

    public SnifferRenderer(EntityRendererProvider.Context param0) {
        super(param0, new SnifferModel<>(param0.bakeLayer(ModelLayers.SNIFFER)), 1.1F);
    }

    public ResourceLocation getTextureLocation(Sniffer param0) {
        return SNIFFER_LOCATION;
    }
}

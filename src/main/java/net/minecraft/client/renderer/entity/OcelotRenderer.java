package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.OcelotModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OcelotRenderer extends MobRenderer<Ocelot, OcelotModel<Ocelot>> {
    private static final ResourceLocation CAT_OCELOT_LOCATION = new ResourceLocation("textures/entity/cat/ocelot.png");

    public OcelotRenderer(EntityRenderDispatcher param0) {
        super(param0, new OcelotModel<>(0.0F), 0.4F);
    }

    protected ResourceLocation getTextureLocation(Ocelot param0) {
        return CAT_OCELOT_LOCATION;
    }
}

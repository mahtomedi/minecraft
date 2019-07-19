package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.SnowGolemModel;
import net.minecraft.client.renderer.entity.layers.SnowGolemHeadLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SnowGolemRenderer extends MobRenderer<SnowGolem, SnowGolemModel<SnowGolem>> {
    private static final ResourceLocation SNOW_GOLEM_LOCATION = new ResourceLocation("textures/entity/snow_golem.png");

    public SnowGolemRenderer(EntityRenderDispatcher param0) {
        super(param0, new SnowGolemModel<>(), 0.5F);
        this.addLayer(new SnowGolemHeadLayer(this));
    }

    protected ResourceLocation getTextureLocation(SnowGolem param0) {
        return SNOW_GOLEM_LOCATION;
    }
}

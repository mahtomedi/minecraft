package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.HoglinModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ZoglinRenderer extends MobRenderer<Zoglin, HoglinModel<Zoglin>> {
    private static final ResourceLocation ZOGLIN_LOCATION = new ResourceLocation("textures/entity/hoglin/zoglin.png");

    public ZoglinRenderer(EntityRenderDispatcher param0) {
        super(param0, new HoglinModel<>(), 0.7F);
    }

    public ResourceLocation getTextureLocation(Zoglin param0) {
        return ZOGLIN_LOCATION;
    }
}

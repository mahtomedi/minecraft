package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.HoglinModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HoglinRenderer extends MobRenderer<Hoglin, HoglinModel> {
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/hoglin/hoglin.png");

    public HoglinRenderer(EntityRenderDispatcher param0) {
        super(param0, new HoglinModel(), 0.7F);
    }

    public ResourceLocation getTextureLocation(Hoglin param0) {
        return TEXTURE_LOCATION;
    }
}

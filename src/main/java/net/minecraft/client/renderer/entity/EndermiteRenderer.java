package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.EndermiteModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EndermiteRenderer extends MobRenderer<Endermite, EndermiteModel<Endermite>> {
    private static final ResourceLocation ENDERMITE_LOCATION = new ResourceLocation("textures/entity/endermite.png");

    public EndermiteRenderer(EntityRenderDispatcher param0) {
        super(param0, new EndermiteModel<>(), 0.3F);
    }

    protected float getFlipDegrees(Endermite param0) {
        return 180.0F;
    }

    public ResourceLocation getTextureLocation(Endermite param0) {
        return ENDERMITE_LOCATION;
    }
}

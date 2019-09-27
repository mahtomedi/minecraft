package net.minecraft.client.renderer.entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TippableArrowRenderer extends ArrowRenderer<Arrow> {
    public static final ResourceLocation NORMAL_ARROW_LOCATION = new ResourceLocation("textures/entity/projectiles/arrow.png");
    public static final ResourceLocation TIPPED_ARROW_LOCATION = new ResourceLocation("textures/entity/projectiles/tipped_arrow.png");

    public TippableArrowRenderer(EntityRenderDispatcher param0) {
        super(param0);
    }

    public ResourceLocation getTextureLocation(Arrow param0) {
        return param0.getColor() > 0 ? TIPPED_ARROW_LOCATION : NORMAL_ARROW_LOCATION;
    }
}

package net.minecraft.client.renderer.entity;

import net.minecraft.client.renderer.entity.layers.StrayClothingLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StrayRenderer extends SkeletonRenderer {
    private static final ResourceLocation STRAY_SKELETON_LOCATION = new ResourceLocation("textures/entity/skeleton/stray.png");

    public StrayRenderer(EntityRenderDispatcher param0) {
        super(param0);
        this.addLayer(new StrayClothingLayer<>(this));
    }

    @Override
    protected ResourceLocation getTextureLocation(AbstractSkeleton param0) {
        return STRAY_SKELETON_LOCATION;
    }
}

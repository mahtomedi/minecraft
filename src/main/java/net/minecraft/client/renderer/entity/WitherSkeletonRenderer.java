package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WitherSkeletonRenderer extends SkeletonRenderer {
    private static final ResourceLocation WITHER_SKELETON_LOCATION = new ResourceLocation("textures/entity/skeleton/wither_skeleton.png");

    public WitherSkeletonRenderer(EntityRenderDispatcher param0) {
        super(param0);
    }

    @Override
    protected ResourceLocation getTextureLocation(AbstractSkeleton param0) {
        return WITHER_SKELETON_LOCATION;
    }

    protected void scale(AbstractSkeleton param0, float param1) {
        RenderSystem.scalef(1.2F, 1.2F, 1.2F);
    }
}

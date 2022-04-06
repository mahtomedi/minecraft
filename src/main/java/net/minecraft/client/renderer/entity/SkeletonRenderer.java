package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SkeletonRenderer extends HumanoidMobRenderer<AbstractSkeleton, SkeletonModel<AbstractSkeleton>> {
    private static final ResourceLocation SKELETON_LOCATION = new ResourceLocation("textures/entity/skeleton/skeleton.png");

    public SkeletonRenderer(EntityRendererProvider.Context param0) {
        this(param0, ModelLayers.SKELETON, ModelLayers.SKELETON_INNER_ARMOR, ModelLayers.SKELETON_OUTER_ARMOR);
    }

    public SkeletonRenderer(EntityRendererProvider.Context param0, ModelLayerLocation param1, ModelLayerLocation param2, ModelLayerLocation param3) {
        super(param0, new SkeletonModel<>(param0.bakeLayer(param1)), 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(this, new SkeletonModel(param0.bakeLayer(param2)), new SkeletonModel(param0.bakeLayer(param3))));
    }

    public ResourceLocation getTextureLocation(AbstractSkeleton param0) {
        return SKELETON_LOCATION;
    }

    protected boolean isShaking(AbstractSkeleton param0) {
        return param0.isShaking();
    }
}

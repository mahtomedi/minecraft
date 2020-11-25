package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StrayClothingLayer<T extends Mob & RangedAttackMob, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final ResourceLocation STRAY_CLOTHES_LOCATION = new ResourceLocation("textures/entity/skeleton/stray_overlay.png");
    private final SkeletonModel<T> layerModel;

    public StrayClothingLayer(RenderLayerParent<T, M> param0, EntityModelSet param1) {
        super(param0);
        this.layerModel = new SkeletonModel<>(param1.bakeLayer(ModelLayers.STRAY_OUTER_LAYER));
    }

    public void render(
        PoseStack param0, MultiBufferSource param1, int param2, T param3, float param4, float param5, float param6, float param7, float param8, float param9
    ) {
        coloredCutoutModelCopyLayerRender(
            this.getParentModel(),
            this.layerModel,
            STRAY_CLOTHES_LOCATION,
            param0,
            param1,
            param2,
            param3,
            param4,
            param5,
            param7,
            param8,
            param9,
            param6,
            1.0F,
            1.0F,
            1.0F
        );
    }
}

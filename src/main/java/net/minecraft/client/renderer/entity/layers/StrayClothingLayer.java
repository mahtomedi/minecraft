package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StrayClothingLayer<T extends Mob & RangedAttackMob, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final ResourceLocation STRAY_CLOTHES_LOCATION = new ResourceLocation("textures/entity/skeleton/stray_overlay.png");
    private final SkeletonModel<T> layerModel = new SkeletonModel<>(0.25F, true);

    public StrayClothingLayer(RenderLayerParent<T, M> param0) {
        super(param0);
    }

    public void render(T param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7) {
        this.getParentModel().copyPropertiesTo(this.layerModel);
        this.layerModel.prepareMobModel(param0, param1, param2, param3);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.bindTexture(STRAY_CLOTHES_LOCATION);
        this.layerModel.render(param0, param1, param2, param4, param5, param6, param7);
    }

    @Override
    public boolean colorsOnDamage() {
        return true;
    }
}

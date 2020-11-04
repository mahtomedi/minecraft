package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractZombieRenderer<T extends Zombie, M extends ZombieModel<T>> extends HumanoidMobRenderer<T, M> {
    private static final ResourceLocation ZOMBIE_LOCATION = new ResourceLocation("textures/entity/zombie/zombie.png");

    protected AbstractZombieRenderer(EntityRendererProvider.Context param0, M param1, M param2, M param3) {
        super(param0, param1, 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(this, param2, param3));
    }

    public ResourceLocation getTextureLocation(Zombie param0) {
        return ZOMBIE_LOCATION;
    }

    protected boolean isShaking(T param0) {
        return param0.isUnderWaterConverting();
    }
}

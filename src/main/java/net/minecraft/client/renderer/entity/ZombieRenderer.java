package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ZombieRenderer extends AbstractZombieRenderer<Zombie, ZombieModel<Zombie>> {
    public ZombieRenderer(EntityRendererProvider.Context param0) {
        this(param0, ModelLayers.ZOMBIE, ModelLayers.ZOMBIE_INNER_ARMOR, ModelLayers.ZOMBIE_OUTER_ARMOR);
    }

    public ZombieRenderer(EntityRendererProvider.Context param0, ModelLayerLocation param1, ModelLayerLocation param2, ModelLayerLocation param3) {
        super(param0, new ZombieModel<>(param0.getLayer(param1)), new ZombieModel<>(param0.getLayer(param2)), new ZombieModel<>(param0.getLayer(param3)));
    }
}

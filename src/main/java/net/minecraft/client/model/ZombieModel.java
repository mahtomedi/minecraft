package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ZombieModel<T extends Zombie> extends AbstractZombieModel<T> {
    public ZombieModel(ModelPart param0) {
        super(param0);
    }

    public boolean isAggressive(T param0) {
        return param0.isAggressive();
    }
}

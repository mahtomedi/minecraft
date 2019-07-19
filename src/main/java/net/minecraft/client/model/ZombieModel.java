package net.minecraft.client.model;

import net.minecraft.world.entity.monster.Zombie;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ZombieModel<T extends Zombie> extends AbstractZombieModel<T> {
    public ZombieModel() {
        this(0.0F, false);
    }

    public ZombieModel(float param0, boolean param1) {
        super(param0, 0.0F, 64, param1 ? 32 : 64);
    }

    protected ZombieModel(float param0, float param1, int param2, int param3) {
        super(param0, param1, param2, param3);
    }

    public boolean isAggressive(T param0) {
        return param0.isAggressive();
    }
}

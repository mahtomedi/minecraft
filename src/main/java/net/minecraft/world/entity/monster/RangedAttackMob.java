package net.minecraft.world.entity.monster;

import net.minecraft.world.entity.LivingEntity;

public interface RangedAttackMob {
    void performRangedAttack(LivingEntity var1, float var2);

    default void performVehicleAttack(float param0) {
    }
}

package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ProjectileWeaponItem;

public class MeleeAttack extends Behavior<Mob> {
    private final double attackRange;
    private final int cooldownBetweenAttacks;

    public MeleeAttack(double param0, int param1) {
        super(
            ImmutableMap.of(
                MemoryModuleType.LOOK_TARGET,
                MemoryStatus.REGISTERED,
                MemoryModuleType.ATTACK_TARGET,
                MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.ATTACK_COOLING_DOWN,
                MemoryStatus.VALUE_ABSENT
            )
        );
        this.attackRange = param0;
        this.cooldownBetweenAttacks = param1;
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, Mob param1) {
        return !param1.isHolding(param0x -> param0x instanceof ProjectileWeaponItem) && BehaviorUtils.isAttackTargetVisibleAndInRange(param1, this.attackRange);
    }

    protected void start(ServerLevel param0, Mob param1, long param2) {
        LivingEntity var0 = param1.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
        BehaviorUtils.lookAtEntity(param1, var0);
        param1.swing(InteractionHand.MAIN_HAND);
        param1.doHurtTarget(var0);
        param1.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_COOLING_DOWN, true, (long)this.cooldownBetweenAttacks);
    }
}

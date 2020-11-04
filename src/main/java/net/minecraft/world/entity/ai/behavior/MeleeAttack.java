package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ProjectileWeaponItem;

public class MeleeAttack extends Behavior<Mob> {
    private final int cooldownBetweenAttacks;

    public MeleeAttack(int param0) {
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
        this.cooldownBetweenAttacks = param0;
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, Mob param1) {
        LivingEntity var0 = this.getAttackTarget(param1);
        return !this.isHoldingUsableProjectileWeapon(param1) && BehaviorUtils.canSee(param1, var0) && BehaviorUtils.isWithinMeleeAttackRange(param1, var0);
    }

    private boolean isHoldingUsableProjectileWeapon(Mob param0) {
        return param0.isHolding(param1 -> {
            Item var0 = param1.getItem();
            return var0 instanceof ProjectileWeaponItem && param0.canFireProjectileWeapon((ProjectileWeaponItem)var0);
        });
    }

    protected void start(ServerLevel param0, Mob param1, long param2) {
        LivingEntity var0 = this.getAttackTarget(param1);
        BehaviorUtils.lookAtEntity(param1, var0);
        param1.swing(InteractionHand.MAIN_HAND);
        param1.doHurtTarget(var0);
        param1.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_COOLING_DOWN, true, (long)this.cooldownBetweenAttacks);
    }

    private LivingEntity getAttackTarget(Mob param0) {
        return param0.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
    }
}

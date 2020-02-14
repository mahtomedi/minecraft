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
    private final int cooldown;
    private int remainingCooldown = 0;

    public MeleeAttack(double param0, int param1) {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT));
        this.attackRange = param0;
        this.cooldown = param1;
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, Mob param1) {
        if (this.remainingCooldown > 0) {
            --this.remainingCooldown;
            return false;
        } else {
            return !this.isHoldingProjectileWeapon(param1) && BehaviorUtils.isAttackTargetVisibleAndInRange(param1, this.attackRange);
        }
    }

    private boolean isHoldingProjectileWeapon(Mob param0) {
        return param0.isHolding(param0x -> param0x instanceof ProjectileWeaponItem);
    }

    protected boolean canStillUse(ServerLevel param0, Mob param1, long param2) {
        return BehaviorUtils.isAttackTargetVisibleAndInRange(param1, this.attackRange);
    }

    protected void start(ServerLevel param0, Mob param1, long param2) {
        LivingEntity var0 = getAttackTarget(param1);
        BehaviorUtils.lookAtEntity(param1, var0);
        this.meleeAttack(param1, var0);
        this.remainingCooldown = this.cooldown;
    }

    private void meleeAttack(Mob param0, LivingEntity param1) {
        param0.swing(InteractionHand.MAIN_HAND);
        param0.doHurtTarget(param1);
    }

    private static LivingEntity getAttackTarget(LivingEntity param0) {
        return param0.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
    }
}

package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CrossbowAttack<E extends Mob & CrossbowAttackMob, T extends LivingEntity> extends Behavior<E> {
    private int attackDelay;
    private CrossbowAttack.CrossbowState crossbowState = CrossbowAttack.CrossbowState.UNCHARGED;

    public CrossbowAttack() {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT), 1200);
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, E param1) {
        LivingEntity var0 = getAttackTarget(param1);
        return param1.isHolding(Items.CROSSBOW) && BehaviorUtils.canSee(param1, var0) && BehaviorUtils.isWithinAttackRange(param1, var0, 0);
    }

    protected boolean canStillUse(ServerLevel param0, E param1, long param2) {
        return param1.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET) && this.checkExtraStartConditions(param0, param1);
    }

    protected void tick(ServerLevel param0, E param1, long param2) {
        LivingEntity var0 = getAttackTarget(param1);
        this.lookAtTarget(param1, var0);
        this.crossbowAttack(param1, var0);
    }

    protected void stop(ServerLevel param0, E param1, long param2) {
        if (param1.isUsingItem()) {
            param1.stopUsingItem();
        }

        if (param1.isHolding(Items.CROSSBOW)) {
            param1.setChargingCrossbow(false);
            CrossbowItem.setCharged(param1.getUseItem(), false);
        }

    }

    private void crossbowAttack(E param0, LivingEntity param1) {
        if (this.crossbowState == CrossbowAttack.CrossbowState.UNCHARGED) {
            param0.startUsingItem(ProjectileUtil.getWeaponHoldingHand(param0, Items.CROSSBOW));
            this.crossbowState = CrossbowAttack.CrossbowState.CHARGING;
            param0.setChargingCrossbow(true);
        } else if (this.crossbowState == CrossbowAttack.CrossbowState.CHARGING) {
            if (!param0.isUsingItem()) {
                this.crossbowState = CrossbowAttack.CrossbowState.UNCHARGED;
            }

            int var0 = param0.getTicksUsingItem();
            ItemStack var1 = param0.getUseItem();
            if (var0 >= CrossbowItem.getChargeDuration(var1)) {
                param0.releaseUsingItem();
                this.crossbowState = CrossbowAttack.CrossbowState.CHARGED;
                this.attackDelay = 20 + param0.getRandom().nextInt(20);
                param0.setChargingCrossbow(false);
            }
        } else if (this.crossbowState == CrossbowAttack.CrossbowState.CHARGED) {
            --this.attackDelay;
            if (this.attackDelay == 0) {
                this.crossbowState = CrossbowAttack.CrossbowState.READY_TO_ATTACK;
            }
        } else if (this.crossbowState == CrossbowAttack.CrossbowState.READY_TO_ATTACK) {
            param0.performRangedAttack(param1, 1.0F);
            ItemStack var2 = param0.getItemInHand(ProjectileUtil.getWeaponHoldingHand(param0, Items.CROSSBOW));
            CrossbowItem.setCharged(var2, false);
            this.crossbowState = CrossbowAttack.CrossbowState.UNCHARGED;
        }

    }

    private void lookAtTarget(Mob param0, LivingEntity param1) {
        param0.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(param1, true));
    }

    private static LivingEntity getAttackTarget(LivingEntity param0) {
        return param0.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
    }

    static enum CrossbowState {
        UNCHARGED,
        CHARGING,
        CHARGED,
        READY_TO_ATTACK;
    }
}

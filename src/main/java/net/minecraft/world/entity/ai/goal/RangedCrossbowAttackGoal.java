package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.util.IntRange;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class RangedCrossbowAttackGoal<T extends Monster & RangedAttackMob & CrossbowAttackMob> extends Goal {
    public static final IntRange PATHFINDING_DELAY_RANGE = new IntRange(20, 40);
    private final T mob;
    private RangedCrossbowAttackGoal.CrossbowState crossbowState = RangedCrossbowAttackGoal.CrossbowState.UNCHARGED;
    private final double speedModifier;
    private final float attackRadiusSqr;
    private int seeTime;
    private int attackDelay;
    private int updatePathDelay;

    public RangedCrossbowAttackGoal(T param0, double param1, float param2) {
        this.mob = param0;
        this.speedModifier = param1;
        this.attackRadiusSqr = param2 * param2;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.isValidTarget() && this.isHoldingCrossbow();
    }

    private boolean isHoldingCrossbow() {
        return this.mob.isHolding(Items.CROSSBOW);
    }

    @Override
    public boolean canContinueToUse() {
        return this.isValidTarget() && (this.canUse() || !this.mob.getNavigation().isDone()) && this.isHoldingCrossbow();
    }

    private boolean isValidTarget() {
        return this.mob.getTarget() != null && this.mob.getTarget().isAlive();
    }

    @Override
    public void stop() {
        super.stop();
        this.mob.setAggressive(false);
        this.mob.setTarget(null);
        this.seeTime = 0;
        if (this.mob.isUsingItem()) {
            this.mob.stopUsingItem();
            this.mob.setChargingCrossbow(false);
            CrossbowItem.setCharged(this.mob.getUseItem(), false);
        }

    }

    @Override
    public void tick() {
        LivingEntity var0 = this.mob.getTarget();
        if (var0 != null) {
            boolean var1 = this.mob.getSensing().canSee(var0);
            boolean var2 = this.seeTime > 0;
            if (var1 != var2) {
                this.seeTime = 0;
            }

            if (var1) {
                ++this.seeTime;
            } else {
                --this.seeTime;
            }

            double var3 = this.mob.distanceToSqr(var0);
            boolean var4 = (var3 > (double)this.attackRadiusSqr || this.seeTime < 5) && this.attackDelay == 0;
            if (var4) {
                --this.updatePathDelay;
                if (this.updatePathDelay <= 0) {
                    this.mob.getNavigation().moveTo(var0, this.canRun() ? this.speedModifier : this.speedModifier * 0.5);
                    this.updatePathDelay = PATHFINDING_DELAY_RANGE.randomValue(this.mob.getRandom());
                }
            } else {
                this.updatePathDelay = 0;
                this.mob.getNavigation().stop();
            }

            this.mob.getLookControl().setLookAt(var0, 30.0F, 30.0F);
            if (this.crossbowState == RangedCrossbowAttackGoal.CrossbowState.UNCHARGED) {
                if (!var4) {
                    this.mob.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.mob, Items.CROSSBOW));
                    this.crossbowState = RangedCrossbowAttackGoal.CrossbowState.CHARGING;
                    this.mob.setChargingCrossbow(true);
                }
            } else if (this.crossbowState == RangedCrossbowAttackGoal.CrossbowState.CHARGING) {
                if (!this.mob.isUsingItem()) {
                    this.crossbowState = RangedCrossbowAttackGoal.CrossbowState.UNCHARGED;
                }

                int var5 = this.mob.getTicksUsingItem();
                ItemStack var6 = this.mob.getUseItem();
                if (var5 >= CrossbowItem.getChargeDuration(var6)) {
                    this.mob.releaseUsingItem();
                    this.crossbowState = RangedCrossbowAttackGoal.CrossbowState.CHARGED;
                    this.attackDelay = 20 + this.mob.getRandom().nextInt(20);
                    this.mob.setChargingCrossbow(false);
                }
            } else if (this.crossbowState == RangedCrossbowAttackGoal.CrossbowState.CHARGED) {
                --this.attackDelay;
                if (this.attackDelay == 0) {
                    this.crossbowState = RangedCrossbowAttackGoal.CrossbowState.READY_TO_ATTACK;
                }
            } else if (this.crossbowState == RangedCrossbowAttackGoal.CrossbowState.READY_TO_ATTACK && var1) {
                this.mob.performRangedAttack(var0, 1.0F);
                ItemStack var7 = this.mob.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this.mob, Items.CROSSBOW));
                CrossbowItem.setCharged(var7, false);
                this.crossbowState = RangedCrossbowAttackGoal.CrossbowState.UNCHARGED;
            }

        }
    }

    private boolean canRun() {
        return this.crossbowState == RangedCrossbowAttackGoal.CrossbowState.UNCHARGED;
    }

    static enum CrossbowState {
        UNCHARGED,
        CHARGING,
        CHARGED,
        READY_TO_ATTACK;
    }
}

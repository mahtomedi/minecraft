package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Items;

public class RangedBowAttackGoal<T extends Monster & RangedAttackMob> extends Goal {
    private final T mob;
    private final double speedModifier;
    private int attackIntervalMin;
    private final float attackRadiusSqr;
    private int attackTime = -1;
    private int seeTime;
    private boolean strafingClockwise;
    private boolean strafingBackwards;
    private int strafingTime = -1;

    public RangedBowAttackGoal(T param0, double param1, int param2, float param3) {
        this.mob = param0;
        this.speedModifier = param1;
        this.attackIntervalMin = param2;
        this.attackRadiusSqr = param3 * param3;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    public void setMinAttackInterval(int param0) {
        this.attackIntervalMin = param0;
    }

    @Override
    public boolean canUse() {
        if (this.mob.getTarget() == null) {
            return false;
        } else if (this.mob.getItemBySlot(EquipmentSlot.HEAD).is(Items.BARREL)) {
            return false;
        } else {
            return this.mob.getTarget().getItemBySlot(EquipmentSlot.HEAD).is(Items.BARREL) && this.mob.getTarget().isCrouching() ? false : this.isHoldingBow();
        }
    }

    protected boolean isHoldingBow() {
        return this.mob.isHolding(Items.BOW);
    }

    @Override
    public boolean canContinueToUse() {
        if (this.mob.getItemBySlot(EquipmentSlot.HEAD).is(Items.BARREL)) {
            return false;
        } else if (this.mob.getTarget() != null
            && this.mob.getTarget().getItemBySlot(EquipmentSlot.HEAD).is(Items.BARREL)
            && this.mob.getTarget().isCrouching()) {
            return false;
        } else {
            return (this.canUse() || !this.mob.getNavigation().isDone()) && this.isHoldingBow();
        }
    }

    @Override
    public void start() {
        super.start();
        this.mob.setAggressive(true);
    }

    @Override
    public void stop() {
        super.stop();
        this.mob.setAggressive(false);
        this.seeTime = 0;
        this.attackTime = -1;
        this.mob.stopUsingItem();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity var0 = this.mob.getTarget();
        if (var0 != null) {
            double var1 = this.mob.distanceToSqr(var0.getX(), var0.getY(), var0.getZ());
            boolean var2 = this.mob.getSensing().hasLineOfSight(var0);
            boolean var3 = this.seeTime > 0;
            if (var2 != var3) {
                this.seeTime = 0;
            }

            if (var2) {
                ++this.seeTime;
            } else {
                --this.seeTime;
            }

            if (!(var1 > (double)this.attackRadiusSqr) && this.seeTime >= 20) {
                this.mob.getNavigation().stop();
                ++this.strafingTime;
            } else {
                this.mob.getNavigation().moveTo(var0, this.speedModifier);
                this.strafingTime = -1;
            }

            if (this.strafingTime >= 20) {
                if ((double)this.mob.getRandom().nextFloat() < 0.3) {
                    this.strafingClockwise = !this.strafingClockwise;
                }

                if ((double)this.mob.getRandom().nextFloat() < 0.3) {
                    this.strafingBackwards = !this.strafingBackwards;
                }

                this.strafingTime = 0;
            }

            if (this.strafingTime > -1) {
                if (var1 > (double)(this.attackRadiusSqr * 0.75F)) {
                    this.strafingBackwards = false;
                } else if (var1 < (double)(this.attackRadiusSqr * 0.25F)) {
                    this.strafingBackwards = true;
                }

                this.mob.getMoveControl().strafe(this.strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
                this.mob.lookAt(var0, 30.0F, 30.0F);
            } else {
                this.mob.getLookControl().setLookAt(var0, 30.0F, 30.0F);
            }

            if (this.mob.isUsingItem()) {
                if (!var2 && this.seeTime < -60) {
                    this.mob.stopUsingItem();
                } else if (var2) {
                    int var4 = this.mob.getTicksUsingItem();
                    if (var4 >= 20) {
                        this.mob.stopUsingItem();
                        if (this.mob.isPassenger() && this.mob.getRootVehicle() == this.mob.getTarget()) {
                            this.mob.performVehicleAttack(BowItem.getPowerForTime(var4));
                        } else {
                            this.mob.performRangedAttack(var0, BowItem.getPowerForTime(var4));
                        }

                        this.attackTime = this.attackIntervalMin;
                    }
                }
            } else if (--this.attackTime <= 0 && this.seeTime >= -60) {
                this.mob.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.mob, Items.BOW));
            }

        }
    }
}

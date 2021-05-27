package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.RangedAttackMob;

public class RangedAttackGoal extends Goal {
    private final Mob mob;
    private final RangedAttackMob rangedAttackMob;
    private LivingEntity target;
    private int attackTime = -1;
    private final double speedModifier;
    private int seeTime;
    private final int attackIntervalMin;
    private final int attackIntervalMax;
    private final float attackRadius;
    private final float attackRadiusSqr;

    public RangedAttackGoal(RangedAttackMob param0, double param1, int param2, float param3) {
        this(param0, param1, param2, param2, param3);
    }

    public RangedAttackGoal(RangedAttackMob param0, double param1, int param2, int param3, float param4) {
        if (!(param0 instanceof LivingEntity)) {
            throw new IllegalArgumentException("ArrowAttackGoal requires Mob implements RangedAttackMob");
        } else {
            this.rangedAttackMob = param0;
            this.mob = (Mob)param0;
            this.speedModifier = param1;
            this.attackIntervalMin = param2;
            this.attackIntervalMax = param3;
            this.attackRadius = param4;
            this.attackRadiusSqr = param4 * param4;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }
    }

    @Override
    public boolean canUse() {
        LivingEntity var0 = this.mob.getTarget();
        if (var0 != null && var0.isAlive()) {
            this.target = var0;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse() || !this.mob.getNavigation().isDone();
    }

    @Override
    public void stop() {
        this.target = null;
        this.seeTime = 0;
        this.attackTime = -1;
    }

    @Override
    public void tick() {
        double var0 = this.mob.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());
        boolean var1 = this.mob.getSensing().hasLineOfSight(this.target);
        if (var1) {
            ++this.seeTime;
        } else {
            this.seeTime = 0;
        }

        if (!(var0 > (double)this.attackRadiusSqr) && this.seeTime >= 5) {
            this.mob.getNavigation().stop();
        } else {
            this.mob.getNavigation().moveTo(this.target, this.speedModifier);
        }

        this.mob.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
        if (--this.attackTime == 0) {
            if (!var1) {
                return;
            }

            float var2 = (float)Math.sqrt(var0) / this.attackRadius;
            float var3 = Mth.clamp(var2, 0.1F, 1.0F);
            this.rangedAttackMob.performRangedAttack(this.target, var3);
            this.attackTime = Mth.floor(var2 * (float)(this.attackIntervalMax - this.attackIntervalMin) + (float)this.attackIntervalMin);
        } else if (this.attackTime < 0) {
            this.attackTime = Mth.floor(Mth.lerp(Math.sqrt(var0) / (double)this.attackRadius, (double)this.attackIntervalMin, (double)this.attackIntervalMax));
        }

    }
}

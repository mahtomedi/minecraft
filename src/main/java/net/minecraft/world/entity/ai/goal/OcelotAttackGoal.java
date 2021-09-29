package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public class OcelotAttackGoal extends Goal {
    private final Mob mob;
    private LivingEntity target;
    private int attackTime;

    public OcelotAttackGoal(Mob param0) {
        this.mob = param0;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity var0 = this.mob.getTarget();
        if (var0 == null) {
            return false;
        } else {
            this.target = var0;
            return true;
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.target.isAlive()) {
            return false;
        } else if (this.mob.distanceToSqr(this.target) > 225.0) {
            return false;
        } else {
            return !this.mob.getNavigation().isDone() || this.canUse();
        }
    }

    @Override
    public void stop() {
        this.target = null;
        this.mob.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.mob.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
        double var0 = (double)(this.mob.getBbWidth() * 2.0F * this.mob.getBbWidth() * 2.0F);
        double var1 = this.mob.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());
        double var2 = 0.8;
        if (var1 > var0 && var1 < 16.0) {
            var2 = 1.33;
        } else if (var1 < 225.0) {
            var2 = 0.6;
        }

        this.mob.getNavigation().moveTo(this.target, var2);
        this.attackTime = Math.max(this.attackTime - 1, 0);
        if (!(var1 > var0)) {
            if (this.attackTime <= 0) {
                this.attackTime = 20;
                this.mob.doHurtTarget(this.target);
            }
        }
    }
}

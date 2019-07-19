package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;

public class MeleeAttackGoal extends Goal {
    protected final PathfinderMob mob;
    protected int attackTime;
    private final double speedModifier;
    private final boolean trackTarget;
    private Path path;
    private int timeToRecalcPath;
    private double pathedTargetX;
    private double pathedTargetY;
    private double pathedTargetZ;
    protected final int attackInterval = 20;
    private long lastUpdate;

    public MeleeAttackGoal(PathfinderMob param0, double param1, boolean param2) {
        this.mob = param0;
        this.speedModifier = param1;
        this.trackTarget = param2;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        long var0 = this.mob.level.getGameTime();
        if (var0 - this.lastUpdate < 20L) {
            return false;
        } else {
            this.lastUpdate = var0;
            LivingEntity var1 = this.mob.getTarget();
            if (var1 == null) {
                return false;
            } else if (!var1.isAlive()) {
                return false;
            } else {
                this.path = this.mob.getNavigation().createPath(var1, 0);
                if (this.path != null) {
                    return true;
                } else {
                    return this.getAttackReachSqr(var1) >= this.mob.distanceToSqr(var1.x, var1.getBoundingBox().minY, var1.z);
                }
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity var0 = this.mob.getTarget();
        if (var0 == null) {
            return false;
        } else if (!var0.isAlive()) {
            return false;
        } else if (!this.trackTarget) {
            return !this.mob.getNavigation().isDone();
        } else if (!this.mob.isWithinRestriction(new BlockPos(var0))) {
            return false;
        } else {
            return !(var0 instanceof Player) || !var0.isSpectator() && !((Player)var0).isCreative();
        }
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(this.path, this.speedModifier);
        this.mob.setAggressive(true);
        this.timeToRecalcPath = 0;
    }

    @Override
    public void stop() {
        LivingEntity var0 = this.mob.getTarget();
        if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(var0)) {
            this.mob.setTarget(null);
        }

        this.mob.setAggressive(false);
        this.mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        LivingEntity var0 = this.mob.getTarget();
        this.mob.getLookControl().setLookAt(var0, 30.0F, 30.0F);
        double var1 = this.mob.distanceToSqr(var0.x, var0.getBoundingBox().minY, var0.z);
        --this.timeToRecalcPath;
        if ((this.trackTarget || this.mob.getSensing().canSee(var0))
            && this.timeToRecalcPath <= 0
            && (
                this.pathedTargetX == 0.0 && this.pathedTargetY == 0.0 && this.pathedTargetZ == 0.0
                    || var0.distanceToSqr(this.pathedTargetX, this.pathedTargetY, this.pathedTargetZ) >= 1.0
                    || this.mob.getRandom().nextFloat() < 0.05F
            )) {
            this.pathedTargetX = var0.x;
            this.pathedTargetY = var0.getBoundingBox().minY;
            this.pathedTargetZ = var0.z;
            this.timeToRecalcPath = 4 + this.mob.getRandom().nextInt(7);
            if (var1 > 1024.0) {
                this.timeToRecalcPath += 10;
            } else if (var1 > 256.0) {
                this.timeToRecalcPath += 5;
            }

            if (!this.mob.getNavigation().moveTo(var0, this.speedModifier)) {
                this.timeToRecalcPath += 15;
            }
        }

        this.attackTime = Math.max(this.attackTime - 1, 0);
        this.checkAndPerformAttack(var0, var1);
    }

    protected void checkAndPerformAttack(LivingEntity param0, double param1) {
        double var0 = this.getAttackReachSqr(param0);
        if (param1 <= var0 && this.attackTime <= 0) {
            this.attackTime = 20;
            this.mob.swing(InteractionHand.MAIN_HAND);
            this.mob.doHurtTarget(param0);
        }

    }

    protected double getAttackReachSqr(LivingEntity param0) {
        return (double)(this.mob.getBbWidth() * 2.0F * this.mob.getBbWidth() * 2.0F + param0.getBbWidth());
    }
}

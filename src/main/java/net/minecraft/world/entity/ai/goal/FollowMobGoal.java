package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

public class FollowMobGoal extends Goal {
    private final Mob mob;
    private final Predicate<Mob> followPredicate;
    @Nullable
    private Mob followingMob;
    private final double speedModifier;
    private final PathNavigation navigation;
    private int timeToRecalcPath;
    private final float stopDistance;
    private float oldWaterCost;
    private final float areaSize;

    public FollowMobGoal(Mob param0, double param1, float param2, float param3) {
        this.mob = param0;
        this.followPredicate = param1x -> param1x != null && param0.getClass() != param1x.getClass();
        this.speedModifier = param1;
        this.navigation = param0.getNavigation();
        this.stopDistance = param2;
        this.areaSize = param3;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        if (!(param0.getNavigation() instanceof GroundPathNavigation) && !(param0.getNavigation() instanceof FlyingPathNavigation)) {
            throw new IllegalArgumentException("Unsupported mob type for FollowMobGoal");
        }
    }

    @Override
    public boolean canUse() {
        List<Mob> var0 = this.mob.level.getEntitiesOfClass(Mob.class, this.mob.getBoundingBox().inflate((double)this.areaSize), this.followPredicate);
        if (!var0.isEmpty()) {
            for(Mob var1 : var0) {
                if (!var1.isInvisible()) {
                    this.followingMob = var1;
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.followingMob != null
            && !this.navigation.isDone()
            && this.mob.distanceToSqr(this.followingMob) > (double)(this.stopDistance * this.stopDistance);
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = this.mob.getPathfindingMalus(BlockPathTypes.WATER);
        this.mob.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
    }

    @Override
    public void stop() {
        this.followingMob = null;
        this.navigation.stop();
        this.mob.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
    }

    @Override
    public void tick() {
        if (this.followingMob != null && !this.mob.isLeashed()) {
            this.mob.getLookControl().setLookAt(this.followingMob, 10.0F, (float)this.mob.getMaxHeadXRot());
            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = this.adjustedTickDelay(10);
                double var0 = this.mob.getX() - this.followingMob.getX();
                double var1 = this.mob.getY() - this.followingMob.getY();
                double var2 = this.mob.getZ() - this.followingMob.getZ();
                double var3 = var0 * var0 + var1 * var1 + var2 * var2;
                if (!(var3 <= (double)(this.stopDistance * this.stopDistance))) {
                    this.navigation.moveTo(this.followingMob, this.speedModifier);
                } else {
                    this.navigation.stop();
                    LookControl var4 = this.followingMob.getLookControl();
                    if (var3 <= (double)this.stopDistance
                        || var4.getWantedX() == this.mob.getX() && var4.getWantedY() == this.mob.getY() && var4.getWantedZ() == this.mob.getZ()) {
                        double var5 = this.followingMob.getX() - this.mob.getX();
                        double var6 = this.followingMob.getZ() - this.mob.getZ();
                        this.navigation.moveTo(this.mob.getX() - var5, this.mob.getY(), this.mob.getZ() - var6, this.speedModifier);
                    }

                }
            }
        }
    }
}

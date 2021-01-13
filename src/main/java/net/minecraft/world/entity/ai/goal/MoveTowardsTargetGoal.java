package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.phys.Vec3;

public class MoveTowardsTargetGoal extends Goal {
    private final PathfinderMob mob;
    private LivingEntity target;
    private double wantedX;
    private double wantedY;
    private double wantedZ;
    private final double speedModifier;
    private final float within;

    public MoveTowardsTargetGoal(PathfinderMob param0, double param1, float param2) {
        this.mob = param0;
        this.speedModifier = param1;
        this.within = param2;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        this.target = this.mob.getTarget();
        if (this.target == null) {
            return false;
        } else if (this.target.distanceToSqr(this.mob) > (double)(this.within * this.within)) {
            return false;
        } else {
            Vec3 var0 = RandomPos.getPosTowards(this.mob, 16, 7, this.target.position());
            if (var0 == null) {
                return false;
            } else {
                this.wantedX = var0.x;
                this.wantedY = var0.y;
                this.wantedZ = var0.z;
                return true;
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        return !this.mob.getNavigation().isDone() && this.target.isAlive() && this.target.distanceToSqr(this.mob) < (double)(this.within * this.within);
    }

    @Override
    public void stop() {
        this.target = null;
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
    }
}

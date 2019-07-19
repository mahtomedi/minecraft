package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.phys.Vec3;

public class MoveTowardsRestrictionGoal extends Goal {
    private final PathfinderMob mob;
    private double wantedX;
    private double wantedY;
    private double wantedZ;
    private final double speedModifier;

    public MoveTowardsRestrictionGoal(PathfinderMob param0, double param1) {
        this.mob = param0;
        this.speedModifier = param1;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.mob.isWithinRestriction()) {
            return false;
        } else {
            BlockPos var0 = this.mob.getRestrictCenter();
            Vec3 var1 = RandomPos.getPosTowards(this.mob, 16, 7, new Vec3((double)var0.getX(), (double)var0.getY(), (double)var0.getZ()));
            if (var1 == null) {
                return false;
            } else {
                this.wantedX = var1.x;
                this.wantedY = var1.y;
                this.wantedZ = var1.z;
                return true;
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        return !this.mob.getNavigation().isDone();
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
    }
}

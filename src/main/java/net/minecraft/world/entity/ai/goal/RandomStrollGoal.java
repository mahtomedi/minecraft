package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

public class RandomStrollGoal extends Goal {
    protected final PathfinderMob mob;
    protected double wantedX;
    protected double wantedY;
    protected double wantedZ;
    protected final double speedModifier;
    protected int interval;
    protected boolean forceTrigger;
    private final boolean checkNoActionTime;

    public RandomStrollGoal(PathfinderMob param0, double param1) {
        this(param0, param1, 120);
    }

    public RandomStrollGoal(PathfinderMob param0, double param1, int param2) {
        this(param0, param1, param2, true);
    }

    public RandomStrollGoal(PathfinderMob param0, double param1, int param2, boolean param3) {
        this.mob = param0;
        this.speedModifier = param1;
        this.interval = param2;
        this.checkNoActionTime = param3;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.mob.isVehicle()) {
            return false;
        } else {
            if (!this.forceTrigger) {
                if (this.checkNoActionTime && this.mob.getNoActionTime() >= 100) {
                    return false;
                }

                if (this.mob.getRandom().nextInt(this.interval) != 0) {
                    return false;
                }
            }

            Vec3 var0 = this.getPosition();
            if (var0 == null) {
                return false;
            } else {
                this.wantedX = var0.x;
                this.wantedY = var0.y;
                this.wantedZ = var0.z;
                this.forceTrigger = false;
                return true;
            }
        }
    }

    @Nullable
    protected Vec3 getPosition() {
        return DefaultRandomPos.getPos(this.mob, 10, 7);
    }

    @Override
    public boolean canContinueToUse() {
        return !this.mob.getNavigation().isDone() && !this.mob.isVehicle();
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
    }

    @Override
    public void stop() {
        this.mob.getNavigation().stop();
        super.stop();
    }

    public void trigger() {
        this.forceTrigger = true;
    }

    public void setInterval(int param0) {
        this.interval = param0;
    }
}

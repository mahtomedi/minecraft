package net.minecraft.world.entity.ai.goal;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.phys.Vec3;

public class FollowBoatGoal extends Goal {
    private int timeToRecalcPath;
    private final PathfinderMob mob;
    private Player following;
    private BoatGoals currentGoal;

    public FollowBoatGoal(PathfinderMob param0) {
        this.mob = param0;
    }

    @Override
    public boolean canUse() {
        List<Boat> var0 = this.mob.level.getEntitiesOfClass(Boat.class, this.mob.getBoundingBox().inflate(5.0));
        boolean var1 = false;

        for(Boat var2 : var0) {
            Entity var3 = var2.getControllingPassenger();
            if (var3 instanceof Player && (Mth.abs(((Player)var3).xxa) > 0.0F || Mth.abs(((Player)var3).zza) > 0.0F)) {
                var1 = true;
                break;
            }
        }

        return this.following != null && (Mth.abs(this.following.xxa) > 0.0F || Mth.abs(this.following.zza) > 0.0F) || var1;
    }

    @Override
    public boolean isInterruptable() {
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.following != null && this.following.isPassenger() && (Mth.abs(this.following.xxa) > 0.0F || Mth.abs(this.following.zza) > 0.0F);
    }

    @Override
    public void start() {
        for(Boat var1 : this.mob.level.getEntitiesOfClass(Boat.class, this.mob.getBoundingBox().inflate(5.0))) {
            if (var1.getControllingPassenger() != null && var1.getControllingPassenger() instanceof Player) {
                this.following = (Player)var1.getControllingPassenger();
                break;
            }
        }

        this.timeToRecalcPath = 0;
        this.currentGoal = BoatGoals.GO_TO_BOAT;
    }

    @Override
    public void stop() {
        this.following = null;
    }

    @Override
    public void tick() {
        boolean var0 = Mth.abs(this.following.xxa) > 0.0F || Mth.abs(this.following.zza) > 0.0F;
        float var1 = this.currentGoal == BoatGoals.GO_IN_BOAT_DIRECTION ? (var0 ? 0.01F : 0.0F) : 0.015F;
        this.mob.moveRelative(var1, new Vec3((double)this.mob.xxa, (double)this.mob.yya, (double)this.mob.zza));
        this.mob.move(MoverType.SELF, this.mob.getDeltaMovement());
        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = 10;
            if (this.currentGoal == BoatGoals.GO_TO_BOAT) {
                BlockPos var2 = this.following.blockPosition().relative(this.following.getDirection().getOpposite());
                var2 = var2.offset(0, -1, 0);
                this.mob.getNavigation().moveTo((double)var2.getX(), (double)var2.getY(), (double)var2.getZ(), 1.0);
                if (this.mob.distanceTo(this.following) < 4.0F) {
                    this.timeToRecalcPath = 0;
                    this.currentGoal = BoatGoals.GO_IN_BOAT_DIRECTION;
                }
            } else if (this.currentGoal == BoatGoals.GO_IN_BOAT_DIRECTION) {
                Direction var3 = this.following.getMotionDirection();
                BlockPos var4 = this.following.blockPosition().relative(var3, 10);
                this.mob.getNavigation().moveTo((double)var4.getX(), (double)(var4.getY() - 1), (double)var4.getZ(), 1.0);
                if (this.mob.distanceTo(this.following) > 12.0F) {
                    this.timeToRecalcPath = 0;
                    this.currentGoal = BoatGoals.GO_TO_BOAT;
                }
            }

        }
    }
}

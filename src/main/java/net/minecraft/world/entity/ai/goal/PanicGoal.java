package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;

public class PanicGoal extends Goal {
    public static final int WATER_CHECK_DISTANCE_VERTICAL = 1;
    protected final PathfinderMob mob;
    protected final double speedModifier;
    protected double posX;
    protected double posY;
    protected double posZ;
    protected boolean isRunning;

    public PanicGoal(PathfinderMob param0, double param1) {
        this.mob = param0;
        this.speedModifier = param1;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!this.shouldPanic()) {
            return false;
        } else {
            if (this.mob.isOnFire()) {
                BlockPos var0 = this.lookForWater(this.mob.level, this.mob, 5);
                if (var0 != null) {
                    this.posX = (double)var0.getX();
                    this.posY = (double)var0.getY();
                    this.posZ = (double)var0.getZ();
                    return true;
                }
            }

            return this.findRandomPosition();
        }
    }

    protected boolean shouldPanic() {
        return this.mob.getLastHurtByMob() != null || this.mob.isFreezing() || this.mob.isOnFire();
    }

    protected boolean findRandomPosition() {
        Vec3 var0 = DefaultRandomPos.getPos(this.mob, 5, 4);
        if (var0 == null) {
            return false;
        } else {
            this.posX = var0.x;
            this.posY = var0.y;
            this.posZ = var0.z;
            return true;
        }
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(this.posX, this.posY, this.posZ, this.speedModifier);
        this.isRunning = true;
    }

    @Override
    public void stop() {
        this.isRunning = false;
    }

    @Override
    public boolean canContinueToUse() {
        return !this.mob.getNavigation().isDone();
    }

    @Nullable
    protected BlockPos lookForWater(BlockGetter param0, Entity param1, int param2) {
        BlockPos var0 = param1.blockPosition();
        return !param0.getBlockState(var0).getCollisionShape(param0, var0).isEmpty()
            ? null
            : BlockPos.findClosestMatch(param1.blockPosition(), param2, 1, param1x -> param0.getFluidState(param1x).is(FluidTags.WATER)).orElse(null);
    }
}

package net.minecraft.world.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.Vec3;

public abstract class PathfinderMob extends Mob {
    protected PathfinderMob(EntityType<? extends PathfinderMob> param0, Level param1) {
        super(param0, param1);
    }

    public float getWalkTargetValue(BlockPos param0) {
        return this.getWalkTargetValue(param0, this.level);
    }

    public float getWalkTargetValue(BlockPos param0, LevelReader param1) {
        return 0.0F;
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor param0, MobSpawnType param1) {
        return this.getWalkTargetValue(this.blockPosition(), param0) >= 0.0F;
    }

    public boolean isPathFinding() {
        return !this.getNavigation().isDone();
    }

    @Override
    protected void tickLeash() {
        super.tickLeash();
        Entity var0 = this.getLeashHolder();
        if (var0 != null && var0.level == this.level) {
            this.restrictTo(var0.blockPosition(), 5);
            float var1 = this.distanceTo(var0);
            if (this instanceof TamableAnimal && ((TamableAnimal)this).isInSittingPose()) {
                if (var1 > 10.0F) {
                    this.dropLeash(true, true);
                }

                return;
            }

            this.onLeashDistance(var1);
            if (var1 > 10.0F) {
                this.dropLeash(true, true);
                this.goalSelector.disableControlFlag(Goal.Flag.MOVE);
            } else if (var1 > 6.0F) {
                double var2 = (var0.getX() - this.getX()) / (double)var1;
                double var3 = (var0.getY() - this.getY()) / (double)var1;
                double var4 = (var0.getZ() - this.getZ()) / (double)var1;
                this.setDeltaMovement(
                    this.getDeltaMovement()
                        .add(Math.copySign(var2 * var2 * 0.4, var2), Math.copySign(var3 * var3 * 0.4, var3), Math.copySign(var4 * var4 * 0.4, var4))
                );
            } else {
                this.goalSelector.enableControlFlag(Goal.Flag.MOVE);
                float var5 = 2.0F;
                Vec3 var6 = new Vec3(var0.getX() - this.getX(), var0.getY() - this.getY(), var0.getZ() - this.getZ())
                    .normalize()
                    .scale((double)Math.max(var1 - 2.0F, 0.0F));
                this.getNavigation().moveTo(this.getX() + var6.x, this.getY() + var6.y, this.getZ() + var6.z, this.followLeashSpeed());
            }
        }

    }

    protected double followLeashSpeed() {
        return 1.0;
    }

    protected void onLeashDistance(float param0) {
    }
}

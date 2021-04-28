package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

public class FollowOwnerGoal extends Goal {
    public static final int TELEPORT_WHEN_DISTANCE_IS = 12;
    private static final int MIN_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 2;
    private static final int MAX_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 3;
    private static final int MAX_VERTICAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 1;
    private final TamableAnimal tamable;
    private LivingEntity owner;
    private final LevelReader level;
    private final double speedModifier;
    private final PathNavigation navigation;
    private int timeToRecalcPath;
    private final float stopDistance;
    private final float startDistance;
    private float oldWaterCost;
    private final boolean canFly;

    public FollowOwnerGoal(TamableAnimal param0, double param1, float param2, float param3, boolean param4) {
        this.tamable = param0;
        this.level = param0.level;
        this.speedModifier = param1;
        this.navigation = param0.getNavigation();
        this.startDistance = param2;
        this.stopDistance = param3;
        this.canFly = param4;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        if (!(param0.getNavigation() instanceof GroundPathNavigation) && !(param0.getNavigation() instanceof FlyingPathNavigation)) {
            throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
        }
    }

    @Override
    public boolean canUse() {
        LivingEntity var0 = this.tamable.getOwner();
        if (var0 == null) {
            return false;
        } else if (var0.isSpectator()) {
            return false;
        } else if (this.tamable.isOrderedToSit()) {
            return false;
        } else if (this.tamable.distanceToSqr(var0) < (double)(this.startDistance * this.startDistance)) {
            return false;
        } else {
            this.owner = var0;
            return true;
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (this.navigation.isDone()) {
            return false;
        } else if (this.tamable.isOrderedToSit()) {
            return false;
        } else {
            return !(this.tamable.distanceToSqr(this.owner) <= (double)(this.stopDistance * this.stopDistance));
        }
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = this.tamable.getPathfindingMalus(BlockPathTypes.WATER);
        this.tamable.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
    }

    @Override
    public void stop() {
        this.owner = null;
        this.navigation.stop();
        this.tamable.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
    }

    @Override
    public void tick() {
        this.tamable.getLookControl().setLookAt(this.owner, 10.0F, (float)this.tamable.getMaxHeadXRot());
        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = 10;
            if (!this.tamable.isLeashed() && !this.tamable.isPassenger()) {
                if (this.tamable.distanceToSqr(this.owner) >= 144.0) {
                    this.teleportToOwner();
                } else {
                    this.navigation.moveTo(this.owner, this.speedModifier);
                }

            }
        }
    }

    private void teleportToOwner() {
        BlockPos var0 = this.owner.blockPosition();

        for(int var1 = 0; var1 < 10; ++var1) {
            int var2 = this.randomIntInclusive(-3, 3);
            int var3 = this.randomIntInclusive(-1, 1);
            int var4 = this.randomIntInclusive(-3, 3);
            boolean var5 = this.maybeTeleportTo(var0.getX() + var2, var0.getY() + var3, var0.getZ() + var4);
            if (var5) {
                return;
            }
        }

    }

    private boolean maybeTeleportTo(int param0, int param1, int param2) {
        if (Math.abs((double)param0 - this.owner.getX()) < 2.0 && Math.abs((double)param2 - this.owner.getZ()) < 2.0) {
            return false;
        } else if (!this.canTeleportTo(new BlockPos(param0, param1, param2))) {
            return false;
        } else {
            this.tamable.moveTo((double)param0 + 0.5, (double)param1, (double)param2 + 0.5, this.tamable.getYRot(), this.tamable.getXRot());
            this.navigation.stop();
            return true;
        }
    }

    private boolean canTeleportTo(BlockPos param0) {
        BlockPathTypes var0 = WalkNodeEvaluator.getBlockPathTypeStatic(this.level, param0.mutable());
        if (var0 != BlockPathTypes.WALKABLE) {
            return false;
        } else {
            BlockState var1 = this.level.getBlockState(param0.below());
            if (!this.canFly && var1.getBlock() instanceof LeavesBlock) {
                return false;
            } else {
                BlockPos var2 = param0.subtract(this.tamable.blockPosition());
                return this.level.noCollision(this.tamable, this.tamable.getBoundingBox().move(var2));
            }
        }
    }

    private int randomIntInclusive(int param0, int param1) {
        return this.tamable.getRandom().nextInt(param1 - param0 + 1) + param0;
    }
}

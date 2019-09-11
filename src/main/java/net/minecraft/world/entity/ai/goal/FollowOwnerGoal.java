package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

public class FollowOwnerGoal extends Goal {
    protected final TamableAnimal tamable;
    private LivingEntity owner;
    protected final LevelReader level;
    private final double speedModifier;
    private final PathNavigation navigation;
    private int timeToRecalcPath;
    private final float stopDistance;
    private final float startDistance;
    private float oldWaterCost;

    public FollowOwnerGoal(TamableAnimal param0, double param1, float param2, float param3) {
        this.tamable = param0;
        this.level = param0.level;
        this.speedModifier = param1;
        this.navigation = param0.getNavigation();
        this.startDistance = param2;
        this.stopDistance = param3;
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
        } else if (this.tamable.isSitting()) {
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
        return !this.navigation.isDone()
            && !this.tamable.isSitting()
            && this.tamable.distanceToSqr(this.owner) > (double)(this.stopDistance * this.stopDistance);
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
                    int var0 = Mth.floor(this.owner.x) - 2;
                    int var1 = Mth.floor(this.owner.z) - 2;
                    int var2 = Mth.floor(this.owner.getBoundingBox().minY);

                    for(int var3 = 0; var3 <= 4; ++var3) {
                        for(int var4 = 0; var4 <= 4; ++var4) {
                            if ((var3 < 1 || var4 < 1 || var3 > 3 || var4 > 3)
                                && this.isTeleportFriendlyBlock(new BlockPos(var0 + var3, var2 - 1, var1 + var4))) {
                                this.tamable
                                    .moveTo(
                                        (double)((float)(var0 + var3) + 0.5F),
                                        (double)var2,
                                        (double)((float)(var1 + var4) + 0.5F),
                                        this.tamable.yRot,
                                        this.tamable.xRot
                                    );
                                this.navigation.stop();
                                return;
                            }
                        }
                    }
                } else {
                    this.navigation.moveTo(this.owner, this.speedModifier);
                }

            }
        }
    }

    protected boolean isTeleportFriendlyBlock(BlockPos param0) {
        BlockState var0 = this.level.getBlockState(param0);
        return var0.isValidSpawn(this.level, param0, this.tamable.getType())
            && this.level.isEmptyBlock(param0.above())
            && this.level.isEmptyBlock(param0.above(2));
    }
}

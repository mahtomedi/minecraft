package net.minecraft.world.level.pathfinder;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class SwimNodeEvaluator extends NodeEvaluator {
    private final boolean allowBreaching;

    public SwimNodeEvaluator(boolean param0) {
        this.allowBreaching = param0;
    }

    @Override
    public Node getStart() {
        return super.getNode(
            Mth.floor(this.mob.getBoundingBox().minX), Mth.floor(this.mob.getBoundingBox().minY + 0.5), Mth.floor(this.mob.getBoundingBox().minZ)
        );
    }

    @Override
    public Target getGoal(double param0, double param1, double param2) {
        return new Target(
            super.getNode(
                Mth.floor(param0 - (double)(this.mob.getBbWidth() / 2.0F)), Mth.floor(param1 + 0.5), Mth.floor(param2 - (double)(this.mob.getBbWidth() / 2.0F))
            )
        );
    }

    @Override
    public int getNeighbors(Node[] param0, Node param1) {
        int var0 = 0;

        for(Direction var1 : Direction.values()) {
            Node var2 = this.getWaterNode(param1.x + var1.getStepX(), param1.y + var1.getStepY(), param1.z + var1.getStepZ());
            if (var2 != null && !var2.closed) {
                param0[var0++] = var2;
            }
        }

        return var0;
    }

    @Override
    public BlockPathTypes getBlockPathType(
        BlockGetter param0, int param1, int param2, int param3, Mob param4, int param5, int param6, int param7, boolean param8, boolean param9
    ) {
        return this.getBlockPathType(param0, param1, param2, param3);
    }

    @Override
    public BlockPathTypes getBlockPathType(BlockGetter param0, int param1, int param2, int param3) {
        BlockPos var0 = new BlockPos(param1, param2, param3);
        FluidState var1 = param0.getFluidState(var0);
        BlockState var2 = param0.getBlockState(var0);
        if (var1.isEmpty() && var2.isPathfindable(param0, var0.below(), PathComputationType.WATER) && var2.isAir()) {
            return BlockPathTypes.BREACH;
        } else {
            return var1.is(FluidTags.WATER) && var2.isPathfindable(param0, var0, PathComputationType.WATER) ? BlockPathTypes.WATER : BlockPathTypes.BLOCKED;
        }
    }

    @Nullable
    private Node getWaterNode(int param0, int param1, int param2) {
        BlockPathTypes var0 = this.isFree(param0, param1, param2);
        return (!this.allowBreaching || var0 != BlockPathTypes.BREACH) && var0 != BlockPathTypes.WATER ? null : this.getNode(param0, param1, param2);
    }

    @Nullable
    @Override
    protected Node getNode(int param0, int param1, int param2) {
        Node var0 = null;
        BlockPathTypes var1 = this.getBlockPathType(this.mob.level, param0, param1, param2);
        float var2 = this.mob.getPathfindingMalus(var1);
        if (var2 >= 0.0F) {
            var0 = super.getNode(param0, param1, param2);
            var0.type = var1;
            var0.costMalus = Math.max(var0.costMalus, var2);
            if (this.level.getFluidState(new BlockPos(param0, param1, param2)).isEmpty()) {
                var0.costMalus += 8.0F;
            }
        }

        return var1 == BlockPathTypes.OPEN ? var0 : var0;
    }

    private BlockPathTypes isFree(int param0, int param1, int param2) {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();

        for(int var1 = param0; var1 < param0 + this.entityWidth; ++var1) {
            for(int var2 = param1; var2 < param1 + this.entityHeight; ++var2) {
                for(int var3 = param2; var3 < param2 + this.entityDepth; ++var3) {
                    FluidState var4 = this.level.getFluidState(var0.set(var1, var2, var3));
                    BlockState var5 = this.level.getBlockState(var0.set(var1, var2, var3));
                    if (var4.isEmpty() && var5.isPathfindable(this.level, var0.below(), PathComputationType.WATER) && var5.isAir()) {
                        return BlockPathTypes.BREACH;
                    }

                    if (!var4.is(FluidTags.WATER)) {
                        return BlockPathTypes.BLOCKED;
                    }
                }
            }
        }

        BlockState var6 = this.level.getBlockState(var0);
        return var6.isPathfindable(this.level, var0, PathComputationType.WATER) ? BlockPathTypes.WATER : BlockPathTypes.BLOCKED;
    }
}

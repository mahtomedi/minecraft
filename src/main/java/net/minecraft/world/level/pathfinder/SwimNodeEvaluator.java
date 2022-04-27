package net.minecraft.world.level.pathfinder;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class SwimNodeEvaluator extends NodeEvaluator {
    private final boolean allowBreaching;
    private final Long2ObjectMap<BlockPathTypes> pathTypesByPosCache = new Long2ObjectOpenHashMap<>();

    public SwimNodeEvaluator(boolean param0) {
        this.allowBreaching = param0;
    }

    @Override
    public void prepare(PathNavigationRegion param0, Mob param1) {
        super.prepare(param0, param1);
        this.pathTypesByPosCache.clear();
    }

    @Override
    public void done() {
        super.done();
        this.pathTypesByPosCache.clear();
    }

    @Nullable
    @Override
    public Node getStart() {
        return super.getNode(
            Mth.floor(this.mob.getBoundingBox().minX), Mth.floor(this.mob.getBoundingBox().minY + 0.5), Mth.floor(this.mob.getBoundingBox().minZ)
        );
    }

    @Nullable
    @Override
    public Target getGoal(double param0, double param1, double param2) {
        return this.getTargetFromNode(super.getNode(Mth.floor(param0), Mth.floor(param1), Mth.floor(param2)));
    }

    @Override
    public int getNeighbors(Node[] param0, Node param1) {
        int var0 = 0;
        Map<Direction, Node> var1 = Maps.newEnumMap(Direction.class);

        for(Direction var2 : Direction.values()) {
            Node var3 = this.getNode(param1.x + var2.getStepX(), param1.y + var2.getStepY(), param1.z + var2.getStepZ());
            var1.put(var2, var3);
            if (this.isNodeValid(var3)) {
                param0[var0++] = var3;
            }
        }

        for(Direction var4 : Direction.Plane.HORIZONTAL) {
            Direction var5 = var4.getClockWise();
            Node var6 = this.getNode(param1.x + var4.getStepX() + var5.getStepX(), param1.y, param1.z + var4.getStepZ() + var5.getStepZ());
            if (this.isDiagonalNodeValid(var6, var1.get(var4), var1.get(var5))) {
                param0[var0++] = var6;
            }
        }

        return var0;
    }

    protected boolean isNodeValid(@Nullable Node param0) {
        return param0 != null && !param0.closed;
    }

    protected boolean isDiagonalNodeValid(@Nullable Node param0, @Nullable Node param1, @Nullable Node param2) {
        return this.isNodeValid(param0) && param1 != null && param1.costMalus >= 0.0F && param2 != null && param2.costMalus >= 0.0F;
    }

    @Nullable
    @Override
    protected Node getNode(int param0, int param1, int param2) {
        Node var0 = null;
        BlockPathTypes var1 = this.getCachedBlockType(param0, param1, param2);
        if (this.allowBreaching && var1 == BlockPathTypes.BREACH || var1 == BlockPathTypes.WATER) {
            float var2 = this.mob.getPathfindingMalus(var1);
            if (var2 >= 0.0F) {
                var0 = super.getNode(param0, param1, param2);
                if (var0 != null) {
                    var0.type = var1;
                    var0.costMalus = Math.max(var0.costMalus, var2);
                    if (this.level.getFluidState(new BlockPos(param0, param1, param2)).isEmpty()) {
                        var0.costMalus += 8.0F;
                    }
                }
            }
        }

        return var0;
    }

    protected BlockPathTypes getCachedBlockType(int param0, int param1, int param2) {
        return this.pathTypesByPosCache
            .computeIfAbsent(BlockPos.asLong(param0, param1, param2), param3 -> this.getBlockPathType(this.level, param0, param1, param2));
    }

    @Override
    public BlockPathTypes getBlockPathType(BlockGetter param0, int param1, int param2, int param3) {
        return this.getBlockPathType(
            param0, param1, param2, param3, this.mob, this.entityWidth, this.entityHeight, this.entityDepth, this.canOpenDoors(), this.canPassDoors()
        );
    }

    @Override
    public BlockPathTypes getBlockPathType(
        BlockGetter param0, int param1, int param2, int param3, Mob param4, int param5, int param6, int param7, boolean param8, boolean param9
    ) {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();

        for(int var1 = param1; var1 < param1 + param5; ++var1) {
            for(int var2 = param2; var2 < param2 + param6; ++var2) {
                for(int var3 = param3; var3 < param3 + param7; ++var3) {
                    FluidState var4 = param0.getFluidState(var0.set(var1, var2, var3));
                    BlockState var5 = param0.getBlockState(var0.set(var1, var2, var3));
                    if (var4.isEmpty() && var5.isPathfindable(param0, var0.below(), PathComputationType.WATER) && var5.isAir()) {
                        return BlockPathTypes.BREACH;
                    }

                    if (!var4.is(FluidTags.WATER)) {
                        return BlockPathTypes.BLOCKED;
                    }
                }
            }
        }

        BlockState var6 = param0.getBlockState(var0);
        return var6.isPathfindable(param0, var0, PathComputationType.WATER) ? BlockPathTypes.WATER : BlockPathTypes.BLOCKED;
    }
}

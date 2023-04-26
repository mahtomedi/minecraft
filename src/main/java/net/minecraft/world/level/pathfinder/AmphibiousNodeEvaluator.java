package net.minecraft.world.level.pathfinder;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;

public class AmphibiousNodeEvaluator extends WalkNodeEvaluator {
    private final boolean prefersShallowSwimming;
    private float oldWalkableCost;
    private float oldWaterBorderCost;

    public AmphibiousNodeEvaluator(boolean param0) {
        this.prefersShallowSwimming = param0;
    }

    @Override
    public void prepare(PathNavigationRegion param0, Mob param1) {
        super.prepare(param0, param1);
        param1.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
        this.oldWalkableCost = param1.getPathfindingMalus(BlockPathTypes.WALKABLE);
        param1.setPathfindingMalus(BlockPathTypes.WALKABLE, 6.0F);
        this.oldWaterBorderCost = param1.getPathfindingMalus(BlockPathTypes.WATER_BORDER);
        param1.setPathfindingMalus(BlockPathTypes.WATER_BORDER, 4.0F);
    }

    @Override
    public void done() {
        this.mob.setPathfindingMalus(BlockPathTypes.WALKABLE, this.oldWalkableCost);
        this.mob.setPathfindingMalus(BlockPathTypes.WATER_BORDER, this.oldWaterBorderCost);
        super.done();
    }

    @Override
    public Node getStart() {
        return !this.mob.isInWater()
            ? super.getStart()
            : this.getStartNode(
                new BlockPos(
                    Mth.floor(this.mob.getBoundingBox().minX), Mth.floor(this.mob.getBoundingBox().minY + 0.5), Mth.floor(this.mob.getBoundingBox().minZ)
                )
            );
    }

    @Override
    public Target getGoal(double param0, double param1, double param2) {
        return this.getTargetFromNode(this.getNode(Mth.floor(param0), Mth.floor(param1 + 0.5), Mth.floor(param2)));
    }

    @Override
    public int getNeighbors(Node[] param0, Node param1) {
        int var0 = super.getNeighbors(param0, param1);
        BlockPathTypes var1 = this.getCachedBlockType(this.mob, param1.x, param1.y + 1, param1.z);
        BlockPathTypes var2 = this.getCachedBlockType(this.mob, param1.x, param1.y, param1.z);
        int var3;
        if (this.mob.getPathfindingMalus(var1) >= 0.0F && var2 != BlockPathTypes.STICKY_HONEY) {
            var3 = Mth.floor(Math.max(1.0F, this.mob.maxUpStep()));
        } else {
            var3 = 0;
        }

        double var5 = this.getFloorLevel(new BlockPos(param1.x, param1.y, param1.z));
        Node var6 = this.findAcceptedNode(param1.x, param1.y + 1, param1.z, Math.max(0, var3 - 1), var5, Direction.UP, var2);
        Node var7 = this.findAcceptedNode(param1.x, param1.y - 1, param1.z, var3, var5, Direction.DOWN, var2);
        if (this.isVerticalNeighborValid(var6, param1)) {
            param0[var0++] = var6;
        }

        if (this.isVerticalNeighborValid(var7, param1) && var2 != BlockPathTypes.TRAPDOOR) {
            param0[var0++] = var7;
        }

        for(int var8 = 0; var8 < var0; ++var8) {
            Node var9 = param0[var8];
            if (var9.type == BlockPathTypes.WATER && this.prefersShallowSwimming && var9.y < this.mob.level().getSeaLevel() - 10) {
                ++var9.costMalus;
            }
        }

        return var0;
    }

    private boolean isVerticalNeighborValid(@Nullable Node param0, Node param1) {
        return this.isNeighborValid(param0, param1) && param0.type == BlockPathTypes.WATER;
    }

    @Override
    protected boolean isAmphibious() {
        return true;
    }

    @Override
    public BlockPathTypes getBlockPathType(BlockGetter param0, int param1, int param2, int param3) {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();
        BlockPathTypes var1 = getBlockPathTypeRaw(param0, var0.set(param1, param2, param3));
        if (var1 == BlockPathTypes.WATER) {
            for(Direction var2 : Direction.values()) {
                BlockPathTypes var3 = getBlockPathTypeRaw(param0, var0.set(param1, param2, param3).move(var2));
                if (var3 == BlockPathTypes.BLOCKED) {
                    return BlockPathTypes.WATER_BORDER;
                }
            }

            return BlockPathTypes.WATER;
        } else {
            return getBlockPathTypeStatic(param0, var0);
        }
    }
}

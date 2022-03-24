package net.minecraft.world.level.redstone;

import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public interface NeighborUpdater {
    Direction[] UPDATE_ORDER = new Direction[]{Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH};

    void shapeUpdate(Direction var1, BlockState var2, BlockPos var3, BlockPos var4, int var5, int var6);

    void neighborChanged(BlockPos var1, Block var2, BlockPos var3);

    void neighborChanged(BlockState var1, BlockPos var2, Block var3, BlockPos var4, boolean var5);

    default void updateNeighborsAtExceptFromFacing(BlockPos param0, Block param1, @Nullable Direction param2) {
        for(Direction var0 : UPDATE_ORDER) {
            if (var0 != param2) {
                this.neighborChanged(param0.relative(var0), param1, param0);
            }
        }

    }

    static void executeShapeUpdate(LevelAccessor param0, Direction param1, BlockState param2, BlockPos param3, BlockPos param4, int param5, int param6) {
        BlockState var0 = param0.getBlockState(param3);
        BlockState var1 = var0.updateShape(param1, param2, param0, param3, param4);
        Block.updateOrDestroy(var0, var1, param0, param3, param5, param6);
    }

    static void executeUpdate(Level param0, BlockState param1, BlockPos param2, Block param3, BlockPos param4, boolean param5) {
        try {
            param1.neighborChanged(param0, param2, param3, param4, param5);
        } catch (Throwable var9) {
            CrashReport var1 = CrashReport.forThrowable(var9, "Exception while updating neighbours");
            CrashReportCategory var2 = var1.addCategory("Block being updated");
            var2.setDetail("Source block type", () -> {
                try {
                    return String.format("ID #%s (%s // %s)", Registry.BLOCK.getKey(param3), param3.getDescriptionId(), param3.getClass().getCanonicalName());
                } catch (Throwable var2x) {
                    return "ID #" + Registry.BLOCK.getKey(param3);
                }
            });
            CrashReportCategory.populateBlockDetails(var2, param0, param2, param1);
            throw new ReportedException(var1);
        }
    }
}

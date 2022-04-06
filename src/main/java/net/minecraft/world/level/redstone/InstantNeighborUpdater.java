package net.minecraft.world.level.redstone;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class InstantNeighborUpdater implements NeighborUpdater {
    private final Level level;

    public InstantNeighborUpdater(Level param0) {
        this.level = param0;
    }

    @Override
    public void shapeUpdate(Direction param0, BlockState param1, BlockPos param2, BlockPos param3, int param4, int param5) {
        NeighborUpdater.executeShapeUpdate(this.level, param0, param1, param2, param3, param4, param5 - 1);
    }

    @Override
    public void neighborChanged(BlockPos param0, Block param1, BlockPos param2) {
        BlockState var0 = this.level.getBlockState(param0);
        this.neighborChanged(var0, param0, param1, param2, false);
    }

    @Override
    public void neighborChanged(BlockState param0, BlockPos param1, Block param2, BlockPos param3, boolean param4) {
        NeighborUpdater.executeUpdate(this.level, param0, param1, param2, param3, param4);
    }
}

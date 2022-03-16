package net.minecraft.world.level.redstone;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class InstantNeighborUpdater implements NeighborUpdater {
    private final ServerLevel level;

    public InstantNeighborUpdater(ServerLevel param0) {
        this.level = param0;
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

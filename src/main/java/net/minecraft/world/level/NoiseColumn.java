package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class NoiseColumn {
    private final int minY;
    private final BlockState[] column;

    public NoiseColumn(int param0, BlockState[] param1) {
        this.minY = param0;
        this.column = param1;
    }

    public BlockState getBlockState(BlockPos param0) {
        int var0 = param0.getY() - this.minY;
        return var0 >= 0 && var0 < this.column.length ? this.column[var0] : Blocks.AIR.defaultBlockState();
    }
}

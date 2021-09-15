package net.minecraft.world.level;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;

public final class NoiseColumn implements BlockColumn {
    private final int minY;
    private final BlockState[] column;

    public NoiseColumn(int param0, BlockState[] param1) {
        this.minY = param0;
        this.column = param1;
    }

    @Override
    public BlockState getBlock(int param0) {
        int var0 = param0 - this.minY;
        return var0 >= 0 && var0 < this.column.length ? this.column[var0] : Blocks.AIR.defaultBlockState();
    }

    @Override
    public void setBlock(int param0, BlockState param1) {
        int var0 = param0 - this.minY;
        if (var0 >= 0 && var0 < this.column.length) {
            this.column[var0] = param1;
        } else {
            throw new IllegalArgumentException("Outside of column height: " + param0);
        }
    }
}

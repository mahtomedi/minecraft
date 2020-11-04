package net.minecraft.world.level;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public final class NoiseColumn implements BlockGetter {
    private final BlockState[] column;

    public NoiseColumn(BlockState[] param0) {
        this.column = param0;
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos param0) {
        return null;
    }

    @Override
    public BlockState getBlockState(BlockPos param0) {
        int var0 = param0.getY();
        return var0 >= 0 && var0 < this.column.length ? this.column[var0] : Blocks.AIR.defaultBlockState();
    }

    @Override
    public FluidState getFluidState(BlockPos param0) {
        return this.getBlockState(param0).getFluidState();
    }

    @Override
    public int getSectionsCount() {
        return 16;
    }

    @Override
    public int getMinSection() {
        return 0;
    }
}

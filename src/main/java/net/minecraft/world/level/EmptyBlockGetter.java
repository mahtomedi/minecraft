package net.minecraft.world.level;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public enum EmptyBlockGetter implements BlockGetter {
    INSTANCE;

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos param0) {
        return null;
    }

    @Override
    public BlockState getBlockState(BlockPos param0) {
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public FluidState getFluidState(BlockPos param0) {
        return Fluids.EMPTY.defaultFluidState();
    }
}

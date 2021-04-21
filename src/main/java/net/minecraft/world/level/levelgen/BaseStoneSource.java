package net.minecraft.world.level.levelgen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

@FunctionalInterface
public interface BaseStoneSource {
    default BlockState getBaseBlock(BlockPos param0) {
        return this.getBaseBlock(param0.getX(), param0.getY(), param0.getZ());
    }

    BlockState getBaseBlock(int var1, int var2, int var3);
}

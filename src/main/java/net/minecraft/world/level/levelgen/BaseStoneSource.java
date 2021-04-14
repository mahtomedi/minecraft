package net.minecraft.world.level.levelgen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public interface BaseStoneSource {
    default BlockState getBaseStone(BlockPos param0) {
        return this.getBaseStone(param0.getX(), param0.getY(), param0.getZ());
    }

    BlockState getBaseStone(int var1, int var2, int var3);
}

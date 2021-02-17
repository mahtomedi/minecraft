package net.minecraft.world.level.levelgen;

import net.minecraft.world.level.block.state.BlockState;

public interface BaseStoneSource {
    BlockState getBaseStone(int var1, int var2, int var3, NoiseGeneratorSettings var4);
}

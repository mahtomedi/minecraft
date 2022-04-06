package net.minecraft.world.level.block;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public class NetherVines {
    private static final double BONEMEAL_GROW_PROBABILITY_DECREASE_RATE = 0.826;
    public static final double GROW_PER_TICK_PROBABILITY = 0.1;

    public static boolean isValidGrowthState(BlockState param0) {
        return param0.isAir();
    }

    public static int getBlocksToGrowWhenBonemealed(RandomSource param0) {
        double var0 = 1.0;

        int var1;
        for(var1 = 0; param0.nextDouble() < var0; ++var1) {
            var0 *= 0.826;
        }

        return var1;
    }
}

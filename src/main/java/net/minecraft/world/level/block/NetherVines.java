package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.world.level.block.state.BlockState;

public class NetherVines {
    public static boolean isValidGrowthState(BlockState param0) {
        return param0.isAir();
    }

    public static int getBlocksToGrowWhenBonemealed(Random param0) {
        double var0 = 1.0;

        int var1;
        for(var1 = 0; param0.nextDouble() < var0; ++var1) {
            var0 *= 0.94;
        }

        return var1;
    }
}

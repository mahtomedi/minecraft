package net.minecraft.world.level.levelgen;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

public interface PositionalRandomFactory {
    default RandomSource at(BlockPos param0) {
        return this.at(param0.getX(), param0.getY(), param0.getZ());
    }

    default RandomSource fromHashOf(ResourceLocation param0) {
        return this.fromHashOf(param0.toString());
    }

    RandomSource fromHashOf(String var1);

    RandomSource at(int var1, int var2, int var3);

    @VisibleForTesting
    void parityConfigString(StringBuilder var1);
}

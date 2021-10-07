package net.minecraft.world.level.levelgen;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public interface PositionalRandomFactory {
    default RandomSource at(BlockPos param0) {
        return this.at(param0.getX(), param0.getY(), param0.getZ());
    }

    default RandomSource at(ResourceLocation param0) {
        return this.at(param0.toString());
    }

    RandomSource at(int var1, int var2, int var3);

    RandomSource at(String var1);
}

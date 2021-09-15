package net.minecraft.world.level.levelgen;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

public class PositionalRandomFactory {
    private final long seed;

    public PositionalRandomFactory(long param0) {
        this.seed = param0;
    }

    public SimpleRandomSource at(BlockPos param0) {
        return this.at(param0.getX(), param0.getY(), param0.getZ());
    }

    public SimpleRandomSource at(int param0, int param1, int param2) {
        long var0 = Mth.getSeed(param0, param1, param2);
        long var1 = var0 ^ this.seed;
        return new SimpleRandomSource(var1);
    }
}

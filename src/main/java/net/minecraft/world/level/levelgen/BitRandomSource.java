package net.minecraft.world.level.levelgen;

import net.minecraft.util.RandomSource;

public interface BitRandomSource extends RandomSource {
    float FLOAT_MULTIPLIER = 5.9604645E-8F;
    double DOUBLE_MULTIPLIER = 1.110223E-16F;

    int next(int var1);

    @Override
    default int nextInt() {
        return this.next(32);
    }

    @Override
    default int nextInt(int param0) {
        if (param0 <= 0) {
            throw new IllegalArgumentException("Bound must be positive");
        } else if ((param0 & param0 - 1) == 0) {
            return (int)((long)param0 * (long)this.next(31) >> 31);
        } else {
            int var0;
            int var1;
            do {
                var0 = this.next(31);
                var1 = var0 % param0;
            } while(var0 - var1 + (param0 - 1) < 0);

            return var1;
        }
    }

    @Override
    default long nextLong() {
        int var0 = this.next(32);
        int var1 = this.next(32);
        long var2 = (long)var0 << 32;
        return var2 + (long)var1;
    }

    @Override
    default boolean nextBoolean() {
        return this.next(1) != 0;
    }

    @Override
    default float nextFloat() {
        return (float)this.next(24) * 5.9604645E-8F;
    }

    @Override
    default double nextDouble() {
        int var0 = this.next(26);
        int var1 = this.next(27);
        long var2 = ((long)var0 << 27) + (long)var1;
        return (double)var2 * 1.110223E-16F;
    }
}

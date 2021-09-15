package net.minecraft.world.level.levelgen;

import net.minecraft.util.Mth;

public class SingleThreadedRandomSource implements BitRandomSource {
    private static final int MODULUS_BITS = 48;
    private static final long MODULUS_MASK = 281474976710655L;
    private static final long MULTIPLIER = 25214903917L;
    private static final long INCREMENT = 11L;
    private long seed;
    private double nextNextGaussian;
    private boolean haveNextNextGaussian;

    public SingleThreadedRandomSource(long param0) {
        this.setSeed(param0);
    }

    @Override
    public RandomSource fork() {
        return new SingleThreadedRandomSource(this.nextLong());
    }

    @Override
    public void setSeed(long param0) {
        this.seed = (param0 ^ 25214903917L) & 281474976710655L;
    }

    @Override
    public int next(int param0) {
        long var0 = this.seed * 25214903917L + 11L & 281474976710655L;
        this.seed = var0;
        return (int)(var0 >> 48 - param0);
    }

    @Override
    public double nextGaussian() {
        if (this.haveNextNextGaussian) {
            this.haveNextNextGaussian = false;
            return this.nextNextGaussian;
        } else {
            double var0;
            double var1;
            double var2;
            do {
                var0 = 2.0 * this.nextDouble() - 1.0;
                var1 = 2.0 * this.nextDouble() - 1.0;
                var2 = Mth.square(var0) + Mth.square(var1);
            } while(var2 >= 1.0 || var2 == 0.0);

            double var3 = Math.sqrt(-2.0 * Math.log(var2) / var2);
            this.nextNextGaussian = var1 * var3;
            this.haveNextNextGaussian = true;
            return var0 * var3;
        }
    }
}

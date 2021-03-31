package net.minecraft.world.level.levelgen;

import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.util.Mth;
import net.minecraft.util.ThreadingDetector;

public class SimpleRandomSource implements RandomSource {
    private static final int MODULUS_BITS = 48;
    private static final long MODULUS_MASK = 281474976710655L;
    private static final long MULTIPLIER = 25214903917L;
    private static final long INCREMENT = 11L;
    private static final float FLOAT_MULTIPLIER = 5.9604645E-8F;
    private static final double DOUBLE_MULTIPLIER = 1.110223E-16F;
    private final AtomicLong seed = new AtomicLong();
    private double nextNextGaussian;
    private boolean haveNextNextGaussian;

    public SimpleRandomSource(long param0) {
        this.setSeed(param0);
    }

    @Override
    public void setSeed(long param0) {
        if (!this.seed.compareAndSet(this.seed.get(), (param0 ^ 25214903917L) & 281474976710655L)) {
            throw ThreadingDetector.makeThreadingException("SimpleRandomSource", null);
        }
    }

    private int next(int param0) {
        long var0 = this.seed.get();
        long var1 = var0 * 25214903917L + 11L & 281474976710655L;
        if (!this.seed.compareAndSet(var0, var1)) {
            throw ThreadingDetector.makeThreadingException("SimpleRandomSource", null);
        } else {
            return (int)(var1 >> 48 - param0);
        }
    }

    @Override
    public int nextInt() {
        return this.next(32);
    }

    @Override
    public int nextInt(int param0) {
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
    public long nextLong() {
        int var0 = this.next(32);
        int var1 = this.next(32);
        long var2 = (long)var0 << 32;
        return var2 + (long)var1;
    }

    @Override
    public boolean nextBoolean() {
        return this.next(1) != 0;
    }

    @Override
    public float nextFloat() {
        return (float)this.next(24) * 5.9604645E-8F;
    }

    @Override
    public double nextDouble() {
        int var0 = this.next(26);
        int var1 = this.next(27);
        long var2 = ((long)var0 << 27) + (long)var1;
        return (double)var2 * 1.110223E-16F;
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

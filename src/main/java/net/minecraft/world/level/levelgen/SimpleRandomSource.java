package net.minecraft.world.level.levelgen;

import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.util.ThreadingDetector;

public class SimpleRandomSource implements RandomSource {
    private final AtomicLong seed = new AtomicLong();
    private boolean haveNextNextGaussian = false;

    public SimpleRandomSource(long param0) {
        this.setSeed(param0);
    }

    public void setSeed(long param0) {
        if (!this.seed.compareAndSet(this.seed.get(), (param0 ^ 25214903917L) & 281474976710655L)) {
            throw ThreadingDetector.makeThreadingException("SimpleRandomSource");
        }
    }

    private int next(int param0) {
        long var0 = this.seed.get();
        long var1 = var0 * 25214903917L + 11L & 281474976710655L;
        if (!this.seed.compareAndSet(var0, var1)) {
            throw ThreadingDetector.makeThreadingException("SimpleRandomSource");
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
    public double nextDouble() {
        return (double)(((long)this.next(26) << 27) + (long)this.next(27)) * 1.110223E-16F;
    }
}

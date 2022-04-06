package net.minecraft.world.level.levelgen;

import com.google.common.annotations.VisibleForTesting;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.ThreadingDetector;

public class LegacyRandomSource implements BitRandomSource {
    private static final int MODULUS_BITS = 48;
    private static final long MODULUS_MASK = 281474976710655L;
    private static final long MULTIPLIER = 25214903917L;
    private static final long INCREMENT = 11L;
    private final AtomicLong seed = new AtomicLong();
    private final MarsagliaPolarGaussian gaussianSource = new MarsagliaPolarGaussian(this);

    public LegacyRandomSource(long param0) {
        this.setSeed(param0);
    }

    @Override
    public RandomSource fork() {
        return new LegacyRandomSource(this.nextLong());
    }

    @Override
    public PositionalRandomFactory forkPositional() {
        return new LegacyRandomSource.LegacyPositionalRandomFactory(this.nextLong());
    }

    @Override
    public void setSeed(long param0) {
        if (!this.seed.compareAndSet(this.seed.get(), (param0 ^ 25214903917L) & 281474976710655L)) {
            throw ThreadingDetector.makeThreadingException("LegacyRandomSource", null);
        } else {
            this.gaussianSource.reset();
        }
    }

    @Override
    public int next(int param0) {
        long var0 = this.seed.get();
        long var1 = var0 * 25214903917L + 11L & 281474976710655L;
        if (!this.seed.compareAndSet(var0, var1)) {
            throw ThreadingDetector.makeThreadingException("LegacyRandomSource", null);
        } else {
            return (int)(var1 >> 48 - param0);
        }
    }

    @Override
    public double nextGaussian() {
        return this.gaussianSource.nextGaussian();
    }

    public static class LegacyPositionalRandomFactory implements PositionalRandomFactory {
        private final long seed;

        public LegacyPositionalRandomFactory(long param0) {
            this.seed = param0;
        }

        @Override
        public RandomSource at(int param0, int param1, int param2) {
            long var0 = Mth.getSeed(param0, param1, param2);
            long var1 = var0 ^ this.seed;
            return new LegacyRandomSource(var1);
        }

        @Override
        public RandomSource fromHashOf(String param0) {
            int var0 = param0.hashCode();
            return new LegacyRandomSource((long)var0 ^ this.seed);
        }

        @VisibleForTesting
        @Override
        public void parityConfigString(StringBuilder param0) {
            param0.append("LegacyPositionalRandomFactory{").append(this.seed).append("}");
        }
    }
}

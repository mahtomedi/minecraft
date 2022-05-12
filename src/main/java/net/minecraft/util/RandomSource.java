package net.minecraft.util;

import io.netty.util.internal.ThreadLocalRandom;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;
import net.minecraft.world.level.levelgen.ThreadSafeLegacyRandomSource;

public interface RandomSource {
    @Deprecated
    double GAUSSIAN_SPREAD_FACTOR = 2.297;

    static RandomSource create() {
        return create(RandomSupport.generateUniqueSeed());
    }

    @Deprecated
    static RandomSource createThreadSafe() {
        return new ThreadSafeLegacyRandomSource(RandomSupport.generateUniqueSeed());
    }

    static RandomSource create(long param0) {
        return new LegacyRandomSource(param0);
    }

    static RandomSource createNewThreadLocalInstance() {
        return new SingleThreadedRandomSource(ThreadLocalRandom.current().nextLong());
    }

    RandomSource fork();

    PositionalRandomFactory forkPositional();

    void setSeed(long var1);

    int nextInt();

    int nextInt(int var1);

    default int nextIntBetweenInclusive(int param0, int param1) {
        return this.nextInt(param1 - param0 + 1) + param0;
    }

    long nextLong();

    boolean nextBoolean();

    float nextFloat();

    double nextDouble();

    double nextGaussian();

    default double triangle(double param0, double param1) {
        return param0 + param1 * (this.nextDouble() - this.nextDouble());
    }

    default void consumeCount(int param0) {
        for(int var0 = 0; var0 < param0; ++var0) {
            this.nextInt();
        }

    }

    default int nextInt(int param0, int param1) {
        if (param0 >= param1) {
            throw new IllegalArgumentException("bound - origin is non positive");
        } else {
            return param0 + this.nextInt(param1 - param0);
        }
    }
}

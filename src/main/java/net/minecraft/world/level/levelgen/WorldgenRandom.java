package net.minecraft.world.level.levelgen;

import java.util.Random;
import java.util.function.LongFunction;

public class WorldgenRandom extends Random implements RandomSource {
    private final RandomSource randomSource;
    private int count;

    public WorldgenRandom(RandomSource param0) {
        super(0L);
        this.randomSource = param0;
    }

    public int getCount() {
        return this.count;
    }

    @Override
    public RandomSource fork() {
        return this.randomSource.fork();
    }

    @Override
    public PositionalRandomFactory forkPositional() {
        return this.randomSource.forkPositional();
    }

    @Override
    public int next(int param0) {
        ++this.count;
        RandomSource var3 = this.randomSource;
        return var3 instanceof LegacyRandomSource var0 ? var0.next(param0) : (int)(this.randomSource.nextLong() >>> 64 - param0);
    }

    @Override
    public synchronized void setSeed(long param0) {
        if (this.randomSource != null) {
            this.randomSource.setSeed(param0);
        }
    }

    public long setDecorationSeed(long param0, int param1, int param2) {
        this.setSeed(param0);
        long var0 = this.nextLong() | 1L;
        long var1 = this.nextLong() | 1L;
        long var2 = (long)param1 * var0 + (long)param2 * var1 ^ param0;
        this.setSeed(var2);
        return var2;
    }

    public void setFeatureSeed(long param0, int param1, int param2) {
        long var0 = param0 + (long)param1 + (long)(10000 * param2);
        this.setSeed(var0);
    }

    public void setLargeFeatureSeed(long param0, int param1, int param2) {
        this.setSeed(param0);
        long var0 = this.nextLong();
        long var1 = this.nextLong();
        long var2 = (long)param1 * var0 ^ (long)param2 * var1 ^ param0;
        this.setSeed(var2);
    }

    public void setLargeFeatureWithSalt(long param0, int param1, int param2, int param3) {
        long var0 = (long)param1 * 341873128712L + (long)param2 * 132897987541L + param0 + (long)param3;
        this.setSeed(var0);
    }

    public static Random seedSlimeChunk(int param0, int param1, long param2, long param3) {
        return new Random(
            param2 + (long)(param0 * param0 * 4987142) + (long)(param0 * 5947611) + (long)(param1 * param1) * 4392871L + (long)(param1 * 389711) ^ param3
        );
    }

    public static enum Algorithm {
        LEGACY(LegacyRandomSource::new),
        XOROSHIRO(XoroshiroRandomSource::new);

        private final LongFunction<RandomSource> constructor;

        private Algorithm(LongFunction<RandomSource> param0) {
            this.constructor = param0;
        }

        public RandomSource newInstance(long param0) {
            return this.constructor.apply(param0);
        }
    }
}

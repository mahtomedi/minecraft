package net.minecraft.world.level.levelgen;

import java.util.Random;

public class WorldgenRandom extends Random implements RandomSource {
    private int count;

    public WorldgenRandom() {
    }

    public WorldgenRandom(long param0) {
        super(param0);
    }

    @Override
    public int next(int param0) {
        ++this.count;
        return super.next(param0);
    }

    public long setBaseChunkSeed(int param0, int param1) {
        long var0 = (long)param0 * 341873128712L + (long)param1 * 132897987541L;
        this.setSeed(var0);
        return var0;
    }

    public long setDecorationSeed(long param0, int param1, int param2) {
        this.setSeed(param0);
        long var0 = this.nextLong() | 1L;
        long var1 = this.nextLong() | 1L;
        long var2 = (long)param1 * var0 + (long)param2 * var1 ^ param0;
        this.setSeed(var2);
        return var2;
    }

    public long setFeatureSeed(long param0, int param1, int param2) {
        long var0 = param0 + (long)param1 + (long)(10000 * param2);
        this.setSeed(var0);
        return var0;
    }

    public long setLargeFeatureSeed(long param0, int param1, int param2) {
        this.setSeed(param0);
        long var0 = this.nextLong();
        long var1 = this.nextLong();
        long var2 = (long)param1 * var0 ^ (long)param2 * var1 ^ param0;
        this.setSeed(var2);
        return var2;
    }

    public long setBaseStoneSeed(long param0, int param1, int param2, int param3) {
        this.setSeed(param0);
        long var0 = this.nextLong();
        long var1 = this.nextLong();
        long var2 = this.nextLong();
        long var3 = (long)param1 * var0 ^ (long)param2 * var1 ^ (long)param3 * var2 ^ param0;
        this.setSeed(var3);
        return var3;
    }

    public long setLargeFeatureWithSalt(long param0, int param1, int param2, int param3) {
        long var0 = (long)param1 * 341873128712L + (long)param2 * 132897987541L + param0 + (long)param3;
        this.setSeed(var0);
        return var0;
    }

    public static Random seedSlimeChunk(int param0, int param1, long param2, long param3) {
        return new Random(
            param2 + (long)(param0 * param0 * 4987142) + (long)(param0 * 5947611) + (long)(param1 * param1) * 4392871L + (long)(param1 * 389711) ^ param3
        );
    }
}

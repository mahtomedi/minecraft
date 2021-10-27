package net.minecraft.world.level.levelgen;

public interface RandomSource {
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

    default void consumeCount(int param0) {
        for(int var0 = 0; var0 < param0; ++var0) {
            this.nextInt();
        }

    }
}

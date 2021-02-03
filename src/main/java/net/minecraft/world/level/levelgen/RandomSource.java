package net.minecraft.world.level.levelgen;

public interface RandomSource {
    int nextInt();

    int nextInt(int var1);

    double nextDouble();

    default void consumeCount(int param0) {
        for(int var0 = 0; var0 < param0; ++var0) {
            this.nextInt();
        }

    }
}

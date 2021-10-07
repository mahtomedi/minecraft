package net.minecraft.world.level.levelgen;

public class Xoroshiro128PlusPlus {
    private long seedLo;
    private long seedHi;

    public Xoroshiro128PlusPlus(RandomSupport.Seed128bit param0) {
        this(param0.seedLo(), param0.seedHi());
    }

    public Xoroshiro128PlusPlus(long param0, long param1) {
        this.seedLo = param0;
        this.seedHi = param1;
        if ((this.seedLo | this.seedHi) == 0L) {
            this.seedLo = -7046029254386353131L;
            this.seedHi = 7640891576956012809L;
        }

    }

    public long nextLong() {
        long var0 = this.seedLo;
        long var1 = this.seedHi;
        long var2 = Long.rotateLeft(var0 + var1, 17) + var0;
        var1 ^= var0;
        this.seedLo = Long.rotateLeft(var0, 49) ^ var1 ^ var1 << 21;
        this.seedHi = Long.rotateLeft(var1, 28);
        return var2;
    }
}

package net.minecraft.world.level.levelgen;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.primitives.Longs;
import net.minecraft.util.Mth;

public class XoroshiroRandomSource implements RandomSource {
    private static final float FLOAT_UNIT = 5.9604645E-8F;
    private static final double DOUBLE_UNIT = 1.110223E-16F;
    private Xoroshiro128PlusPlus randomNumberGenerator;
    private final MarsagliaPolarGaussian gaussianSource = new MarsagliaPolarGaussian(this);

    public XoroshiroRandomSource(long param0) {
        this.setSeed(param0);
    }

    public XoroshiroRandomSource(long param0, long param1) {
        this.randomNumberGenerator = new Xoroshiro128PlusPlus(param0, param1);
    }

    @Override
    public RandomSource fork() {
        return new XoroshiroRandomSource(this.randomNumberGenerator.nextLong(), this.randomNumberGenerator.nextLong());
    }

    @Override
    public PositionalRandomFactory forkPositional() {
        return new XoroshiroRandomSource.XoroshiroPositionalRandomFactory(this.randomNumberGenerator.nextLong(), this.randomNumberGenerator.nextLong());
    }

    @Override
    public void setSeed(long param0) {
        this.randomNumberGenerator = new Xoroshiro128PlusPlus(RandomSupport.upgradeSeedTo128bit(param0));
    }

    @Override
    public int nextInt() {
        return (int)this.randomNumberGenerator.nextLong();
    }

    @Override
    public int nextInt(int param0) {
        if (param0 <= 0) {
            throw new IllegalArgumentException("Bound must be positive");
        } else {
            long var0 = Integer.toUnsignedLong(this.nextInt());
            long var1 = var0 * (long)param0;
            long var2 = var1 & 4294967295L;
            if (var2 < (long)param0) {
                for(int var3 = Integer.remainderUnsigned(~param0 + 1, param0); var2 < (long)var3; var2 = var1 & 4294967295L) {
                    var0 = Integer.toUnsignedLong(this.nextInt());
                    var1 = var0 * (long)param0;
                }
            }

            long var4 = var1 >> 32;
            return (int)var4;
        }
    }

    @Override
    public long nextLong() {
        return this.randomNumberGenerator.nextLong();
    }

    @Override
    public boolean nextBoolean() {
        return (this.randomNumberGenerator.nextLong() & 1L) != 0L;
    }

    @Override
    public float nextFloat() {
        return (float)this.nextBits(24) * 5.9604645E-8F;
    }

    @Override
    public double nextDouble() {
        return (double)this.nextBits(53) * 1.110223E-16F;
    }

    @Override
    public double nextGaussian() {
        return this.gaussianSource.nextGaussian();
    }

    @Override
    public void consumeCount(int param0) {
        for(int var0 = 0; var0 < param0; ++var0) {
            this.randomNumberGenerator.nextLong();
        }

    }

    private long nextBits(int param0) {
        return this.randomNumberGenerator.nextLong() >>> 64 - param0;
    }

    public static class XoroshiroPositionalRandomFactory implements PositionalRandomFactory {
        private static final HashFunction MD5_128 = Hashing.md5();
        private final long seedLo;
        private final long seedHi;

        public XoroshiroPositionalRandomFactory(long param0, long param1) {
            this.seedLo = param0;
            this.seedHi = param1;
        }

        @Override
        public RandomSource at(int param0, int param1, int param2) {
            long var0 = Mth.getSeed(param0, param1, param2);
            long var1 = var0 ^ this.seedLo;
            return new XoroshiroRandomSource(var1, this.seedHi);
        }

        @Override
        public RandomSource fromHashOf(String param0) {
            byte[] var0 = MD5_128.hashString(param0, Charsets.UTF_8).asBytes();
            long var1 = Longs.fromBytes(var0[0], var0[1], var0[2], var0[3], var0[4], var0[5], var0[6], var0[7]);
            long var2 = Longs.fromBytes(var0[8], var0[9], var0[10], var0[11], var0[12], var0[13], var0[14], var0[15]);
            return new XoroshiroRandomSource(var1 ^ this.seedLo, var2 ^ this.seedHi);
        }

        @VisibleForTesting
        @Override
        public void parityConfigString(StringBuilder param0) {
            param0.append("seedLo: ").append(this.seedLo).append(", seedHi: ").append(this.seedHi);
        }
    }
}

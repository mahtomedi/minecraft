package net.minecraft.world.level.levelgen;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.primitives.Longs;
import java.util.concurrent.atomic.AtomicLong;

public final class RandomSupport {
    public static final long GOLDEN_RATIO_64 = -7046029254386353131L;
    public static final long SILVER_RATIO_64 = 7640891576956012809L;
    private static final HashFunction MD5_128 = Hashing.md5();
    private static final AtomicLong SEED_UNIQUIFIER = new AtomicLong(8682522807148012L);

    @VisibleForTesting
    public static long mixStafford13(long param0) {
        param0 = (param0 ^ param0 >>> 30) * -4658895280553007687L;
        param0 = (param0 ^ param0 >>> 27) * -7723592293110705685L;
        return param0 ^ param0 >>> 31;
    }

    public static RandomSupport.Seed128bit upgradeSeedTo128bit(long param0) {
        long var0 = param0 ^ 7640891576956012809L;
        long var1 = var0 + -7046029254386353131L;
        return new RandomSupport.Seed128bit(mixStafford13(var0), mixStafford13(var1));
    }

    public static RandomSupport.Seed128bit seedFromHashOf(String param0) {
        byte[] var0 = MD5_128.hashString(param0, Charsets.UTF_8).asBytes();
        long var1 = Longs.fromBytes(var0[0], var0[1], var0[2], var0[3], var0[4], var0[5], var0[6], var0[7]);
        long var2 = Longs.fromBytes(var0[8], var0[9], var0[10], var0[11], var0[12], var0[13], var0[14], var0[15]);
        return new RandomSupport.Seed128bit(var1, var2);
    }

    public static long generateUniqueSeed() {
        return SEED_UNIQUIFIER.updateAndGet(param0 -> param0 * 1181783497276652981L) ^ System.nanoTime();
    }

    public static record Seed128bit(long seedLo, long seedHi) {
        public RandomSupport.Seed128bit xor(long param0, long param1) {
            return new RandomSupport.Seed128bit(this.seedLo ^ param0, this.seedHi ^ param1);
        }

        public RandomSupport.Seed128bit xor(RandomSupport.Seed128bit param0) {
            return this.xor(param0.seedLo, param0.seedHi);
        }
    }
}

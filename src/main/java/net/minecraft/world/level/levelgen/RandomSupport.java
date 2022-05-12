package net.minecraft.world.level.levelgen;

import com.google.common.annotations.VisibleForTesting;
import java.util.concurrent.atomic.AtomicLong;

public final class RandomSupport {
    public static final long GOLDEN_RATIO_64 = -7046029254386353131L;
    public static final long SILVER_RATIO_64 = 7640891576956012809L;
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

    public static long generateUniqueSeed() {
        return SEED_UNIQUIFIER.updateAndGet(param0 -> param0 * 1181783497276652981L) ^ System.nanoTime();
    }

    public static record Seed128bit(long seedLo, long seedHi) {
    }
}

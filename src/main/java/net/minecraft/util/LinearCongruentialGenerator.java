package net.minecraft.util;

public class LinearCongruentialGenerator {
    public static long next(long param0, long param1) {
        param0 *= param0 * 6364136223846793005L + 1442695040888963407L;
        return param0 + param1;
    }
}

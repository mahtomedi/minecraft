package net.minecraft.util;

public class TimeUtil {
    public static IntRange rangeOfSeconds(int param0, int param1) {
        return new IntRange(param0 * 20, param1 * 20);
    }
}

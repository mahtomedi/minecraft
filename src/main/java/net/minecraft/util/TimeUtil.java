package net.minecraft.util;

import net.minecraft.util.valueproviders.UniformInt;

public class TimeUtil {
    public static UniformInt rangeOfSeconds(int param0, int param1) {
        return UniformInt.of(param0 * 20, param1 * 20);
    }
}

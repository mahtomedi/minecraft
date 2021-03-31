package net.minecraft.util.random;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import net.minecraft.Util;

public class WeightedRandom {
    private WeightedRandom() {
    }

    public static int getTotalWeight(List<? extends WeightedEntry> param0) {
        long var0 = 0L;

        for(WeightedEntry var1 : param0) {
            var0 += (long)var1.getWeight().asInt();
        }

        if (var0 > 2147483647L) {
            throw new IllegalArgumentException("Sum of weights must be <= 2147483647");
        } else {
            return (int)var0;
        }
    }

    public static <T extends WeightedEntry> Optional<T> getRandomItem(Random param0, List<T> param1, int param2) {
        if (param2 < 0) {
            throw (IllegalArgumentException)Util.pauseInIde(new IllegalArgumentException("Negative total weight in getRandomItem"));
        } else if (param2 == 0) {
            return Optional.empty();
        } else {
            int var0 = param0.nextInt(param2);
            return getWeightedItem(param1, var0);
        }
    }

    public static <T extends WeightedEntry> Optional<T> getWeightedItem(List<T> param0, int param1) {
        for(T var0 : param0) {
            param1 -= var0.getWeight().asInt();
            if (param1 < 0) {
                return Optional.of(var0);
            }
        }

        return Optional.empty();
    }

    public static <T extends WeightedEntry> Optional<T> getRandomItem(Random param0, List<T> param1) {
        return getRandomItem(param0, param1, getTotalWeight(param1));
    }
}

package net.minecraft.util;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WeighedRandom {
    private static final Logger LOGGER = LogManager.getLogger();

    public static int getTotalWeight(List<? extends WeighedRandom.WeighedRandomItem> param0) {
        long var0 = 0L;

        for(WeighedRandom.WeighedRandomItem var1 : param0) {
            var0 += (long)var1.weight;
        }

        if (var0 > 2147483647L) {
            throw new IllegalArgumentException("Sum of weights must be <= 2147483647");
        } else {
            return (int)var0;
        }
    }

    public static <T extends WeighedRandom.WeighedRandomItem> Optional<T> getRandomItem(Random param0, List<T> param1, int param2) {
        if (param2 < 0) {
            throw (IllegalArgumentException)Util.pauseInIde(new IllegalArgumentException("Negative total weight in getRandomItem"));
        } else if (param2 == 0) {
            return Optional.empty();
        } else {
            int var0 = param0.nextInt(param2);
            return getWeightedItem(param1, var0);
        }
    }

    public static <T extends WeighedRandom.WeighedRandomItem> Optional<T> getWeightedItem(List<T> param0, int param1) {
        for(T var0 : param0) {
            param1 -= var0.weight;
            if (param1 < 0) {
                return Optional.of(var0);
            }
        }

        return Optional.empty();
    }

    public static <T extends WeighedRandom.WeighedRandomItem> Optional<T> getRandomItem(Random param0, List<T> param1) {
        return getRandomItem(param0, param1, getTotalWeight(param1));
    }

    public static class WeighedRandomItem {
        protected final int weight;

        public WeighedRandomItem(int param0) {
            if (param0 < 0) {
                throw (IllegalArgumentException)Util.pauseInIde(new IllegalArgumentException("Weight should be >= 0"));
            } else {
                if (param0 == 0 && SharedConstants.IS_RUNNING_IN_IDE) {
                    WeighedRandom.LOGGER.warn("Found 0 weight, make sure this is intentional!");
                }

                this.weight = param0;
            }
        }
    }
}

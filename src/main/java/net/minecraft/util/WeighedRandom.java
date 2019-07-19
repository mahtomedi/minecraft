package net.minecraft.util;

import java.util.List;
import java.util.Random;

public class WeighedRandom {
    public static int getTotalWeight(List<? extends WeighedRandom.WeighedRandomItem> param0) {
        int var0 = 0;
        int var1 = 0;

        for(int var2 = param0.size(); var1 < var2; ++var1) {
            WeighedRandom.WeighedRandomItem var3 = param0.get(var1);
            var0 += var3.weight;
        }

        return var0;
    }

    public static <T extends WeighedRandom.WeighedRandomItem> T getRandomItem(Random param0, List<T> param1, int param2) {
        if (param2 <= 0) {
            throw new IllegalArgumentException();
        } else {
            int var0 = param0.nextInt(param2);
            return getWeightedItem(param1, var0);
        }
    }

    public static <T extends WeighedRandom.WeighedRandomItem> T getWeightedItem(List<T> param0, int param1) {
        int var0 = 0;

        for(int var1 = param0.size(); var0 < var1; ++var0) {
            T var2 = param0.get(var0);
            param1 -= var2.weight;
            if (param1 < 0) {
                return var2;
            }
        }

        return null;
    }

    public static <T extends WeighedRandom.WeighedRandomItem> T getRandomItem(Random param0, List<T> param1) {
        return getRandomItem(param0, param1, getTotalWeight(param1));
    }

    public static class WeighedRandomItem {
        protected final int weight;

        public WeighedRandomItem(int param0) {
            this.weight = param0;
        }
    }
}

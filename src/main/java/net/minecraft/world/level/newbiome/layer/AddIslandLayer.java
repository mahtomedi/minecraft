package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.BishopTransformer;

public enum AddIslandLayer implements BishopTransformer {
    INSTANCE;

    @Override
    public int apply(Context param0, int param1, int param2, int param3, int param4, int param5) {
        if (!Layers.isShallowOcean(param5)
            || Layers.isShallowOcean(param4) && Layers.isShallowOcean(param3) && Layers.isShallowOcean(param1) && Layers.isShallowOcean(param2)) {
            if (!Layers.isShallowOcean(param5)
                && (Layers.isShallowOcean(param4) || Layers.isShallowOcean(param1) || Layers.isShallowOcean(param3) || Layers.isShallowOcean(param2))
                && param0.nextRandom(5) == 0) {
                if (Layers.isShallowOcean(param4)) {
                    return param5 == 4 ? 4 : param4;
                }

                if (Layers.isShallowOcean(param1)) {
                    return param5 == 4 ? 4 : param1;
                }

                if (Layers.isShallowOcean(param3)) {
                    return param5 == 4 ? 4 : param3;
                }

                if (Layers.isShallowOcean(param2)) {
                    return param5 == 4 ? 4 : param2;
                }
            }

            return param5;
        } else {
            int var0 = 1;
            int var1 = 1;
            if (!Layers.isShallowOcean(param4) && param0.nextRandom(var0++) == 0) {
                var1 = param4;
            }

            if (!Layers.isShallowOcean(param3) && param0.nextRandom(var0++) == 0) {
                var1 = param3;
            }

            if (!Layers.isShallowOcean(param1) && param0.nextRandom(var0++) == 0) {
                var1 = param1;
            }

            if (!Layers.isShallowOcean(param2) && param0.nextRandom(var0++) == 0) {
                var1 = param2;
            }

            if (param0.nextRandom(3) == 0) {
                return var1;
            } else {
                return var1 == 4 ? 4 : param5;
            }
        }
    }
}

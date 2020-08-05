package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.CastleTransformer;

public enum AddDeepOceanLayer implements CastleTransformer {
    INSTANCE;

    @Override
    public int apply(Context param0, int param1, int param2, int param3, int param4, int param5) {
        if (Layers.isShallowOcean(param5)) {
            int var0 = 0;
            if (Layers.isShallowOcean(param1)) {
                ++var0;
            }

            if (Layers.isShallowOcean(param2)) {
                ++var0;
            }

            if (Layers.isShallowOcean(param4)) {
                ++var0;
            }

            if (Layers.isShallowOcean(param3)) {
                ++var0;
            }

            if (var0 > 3) {
                if (param5 == 44) {
                    return 47;
                }

                if (param5 == 45) {
                    return 48;
                }

                if (param5 == 0) {
                    return 24;
                }

                if (param5 == 46) {
                    return 49;
                }

                if (param5 == 10) {
                    return 50;
                }

                return 24;
            }
        }

        return param5;
    }
}

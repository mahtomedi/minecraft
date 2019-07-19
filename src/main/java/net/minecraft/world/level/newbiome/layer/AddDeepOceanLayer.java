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
                if (param5 == Layers.WARM_OCEAN) {
                    return Layers.DEEP_WARM_OCEAN;
                }

                if (param5 == Layers.LUKEWARM_OCEAN) {
                    return Layers.DEEP_LUKEWARM_OCEAN;
                }

                if (param5 == Layers.OCEAN) {
                    return Layers.DEEP_OCEAN;
                }

                if (param5 == Layers.COLD_OCEAN) {
                    return Layers.DEEP_COLD_OCEAN;
                }

                if (param5 == Layers.FROZEN_OCEAN) {
                    return Layers.DEEP_FROZEN_OCEAN;
                }

                return Layers.DEEP_OCEAN;
            }
        }

        return param5;
    }
}

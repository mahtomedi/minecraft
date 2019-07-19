package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.C0Transformer;
import net.minecraft.world.level.newbiome.layer.traits.CastleTransformer;

public class AddEdgeLayer {
    public static enum CoolWarm implements CastleTransformer {
        INSTANCE;

        @Override
        public int apply(Context param0, int param1, int param2, int param3, int param4, int param5) {
            return param5 != 1 || param1 != 3 && param2 != 3 && param4 != 3 && param3 != 3 && param1 != 4 && param2 != 4 && param4 != 4 && param3 != 4
                ? param5
                : 2;
        }
    }

    public static enum HeatIce implements CastleTransformer {
        INSTANCE;

        @Override
        public int apply(Context param0, int param1, int param2, int param3, int param4, int param5) {
            return param5 != 4 || param1 != 1 && param2 != 1 && param4 != 1 && param3 != 1 && param1 != 2 && param2 != 2 && param4 != 2 && param3 != 2
                ? param5
                : 3;
        }
    }

    public static enum IntroduceSpecial implements C0Transformer {
        INSTANCE;

        @Override
        public int apply(Context param0, int param1) {
            if (!Layers.isShallowOcean(param1) && param0.nextRandom(13) == 0) {
                param1 |= 1 + param0.nextRandom(15) << 8 & 3840;
            }

            return param1;
        }
    }
}

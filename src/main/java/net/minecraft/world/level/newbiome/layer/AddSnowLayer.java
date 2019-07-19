package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.C1Transformer;

public enum AddSnowLayer implements C1Transformer {
    INSTANCE;

    @Override
    public int apply(Context param0, int param1) {
        if (Layers.isShallowOcean(param1)) {
            return param1;
        } else {
            int var0 = param0.nextRandom(6);
            if (var0 == 0) {
                return 4;
            } else {
                return var0 == 1 ? 3 : 1;
            }
        }
    }
}

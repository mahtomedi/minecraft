package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.CastleTransformer;

public enum RemoveTooMuchOceanLayer implements CastleTransformer {
    INSTANCE;

    @Override
    public int apply(Context param0, int param1, int param2, int param3, int param4, int param5) {
        return Layers.isShallowOcean(param5)
                && Layers.isShallowOcean(param1)
                && Layers.isShallowOcean(param2)
                && Layers.isShallowOcean(param4)
                && Layers.isShallowOcean(param3)
                && param0.nextRandom(2) == 0
            ? 1
            : param5;
    }
}

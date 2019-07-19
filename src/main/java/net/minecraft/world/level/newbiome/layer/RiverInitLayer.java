package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.C0Transformer;

public enum RiverInitLayer implements C0Transformer {
    INSTANCE;

    @Override
    public int apply(Context param0, int param1) {
        return Layers.isShallowOcean(param1) ? param1 : param0.nextRandom(299999) + 2;
    }
}

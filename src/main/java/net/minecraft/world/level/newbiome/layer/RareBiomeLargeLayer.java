package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.C1Transformer;

public enum RareBiomeLargeLayer implements C1Transformer {
    INSTANCE;

    @Override
    public int apply(Context param0, int param1) {
        return param0.nextRandom(10) == 0 && param1 == 21 ? 168 : param1;
    }
}

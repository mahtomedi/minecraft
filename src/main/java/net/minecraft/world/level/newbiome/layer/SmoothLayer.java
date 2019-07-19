package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.CastleTransformer;

public enum SmoothLayer implements CastleTransformer {
    INSTANCE;

    @Override
    public int apply(Context param0, int param1, int param2, int param3, int param4, int param5) {
        boolean var0 = param2 == param4;
        boolean var1 = param1 == param3;
        if (var0 == var1) {
            if (var0) {
                return param0.nextRandom(2) == 0 ? param4 : param1;
            } else {
                return param5;
            }
        } else {
            return var0 ? param4 : param1;
        }
    }
}

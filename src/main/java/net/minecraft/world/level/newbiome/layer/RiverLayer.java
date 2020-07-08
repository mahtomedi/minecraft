package net.minecraft.world.level.newbiome.layer;

import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.CastleTransformer;

public enum RiverLayer implements CastleTransformer {
    INSTANCE;

    public static final int RIVER = BuiltinRegistries.BIOME.getId(Biomes.RIVER);

    @Override
    public int apply(Context param0, int param1, int param2, int param3, int param4, int param5) {
        int var0 = riverFilter(param5);
        return var0 == riverFilter(param4) && var0 == riverFilter(param1) && var0 == riverFilter(param2) && var0 == riverFilter(param3) ? -1 : RIVER;
    }

    private static int riverFilter(int param0) {
        return param0 >= 2 ? 2 + (param0 & 1) : param0;
    }
}

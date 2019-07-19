package net.minecraft.world.level.newbiome.layer;

import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer2;
import net.minecraft.world.level.newbiome.layer.traits.DimensionOffset0Transformer;

public enum RiverMixerLayer implements AreaTransformer2, DimensionOffset0Transformer {
    INSTANCE;

    private static final int FROZEN_RIVER = Registry.BIOME.getId(Biomes.FROZEN_RIVER);
    private static final int SNOWY_TUNDRA = Registry.BIOME.getId(Biomes.SNOWY_TUNDRA);
    private static final int MUSHROOM_FIELDS = Registry.BIOME.getId(Biomes.MUSHROOM_FIELDS);
    private static final int MUSHROOM_FIELD_SHORE = Registry.BIOME.getId(Biomes.MUSHROOM_FIELD_SHORE);
    private static final int RIVER = Registry.BIOME.getId(Biomes.RIVER);

    @Override
    public int applyPixel(Context param0, Area param1, Area param2, int param3, int param4) {
        int var0 = param1.get(this.getParentX(param3), this.getParentY(param4));
        int var1 = param2.get(this.getParentX(param3), this.getParentY(param4));
        if (Layers.isOcean(var0)) {
            return var0;
        } else if (var1 == RIVER) {
            if (var0 == SNOWY_TUNDRA) {
                return FROZEN_RIVER;
            } else {
                return var0 != MUSHROOM_FIELDS && var0 != MUSHROOM_FIELD_SHORE ? var1 & 0xFF : MUSHROOM_FIELD_SHORE;
            }
        } else {
            return var0;
        }
    }
}

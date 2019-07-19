package net.minecraft.world.level.newbiome.layer;

import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.CastleTransformer;

public enum ShoreLayer implements CastleTransformer {
    INSTANCE;

    private static final int BEACH = Registry.BIOME.getId(Biomes.BEACH);
    private static final int SNOWY_BEACH = Registry.BIOME.getId(Biomes.SNOWY_BEACH);
    private static final int DESERT = Registry.BIOME.getId(Biomes.DESERT);
    private static final int MOUNTAINS = Registry.BIOME.getId(Biomes.MOUNTAINS);
    private static final int WOODED_MOUNTAINS = Registry.BIOME.getId(Biomes.WOODED_MOUNTAINS);
    private static final int FOREST = Registry.BIOME.getId(Biomes.FOREST);
    private static final int JUNGLE = Registry.BIOME.getId(Biomes.JUNGLE);
    private static final int JUNGLE_EDGE = Registry.BIOME.getId(Biomes.JUNGLE_EDGE);
    private static final int JUNGLE_HILLS = Registry.BIOME.getId(Biomes.JUNGLE_HILLS);
    private static final int BADLANDS = Registry.BIOME.getId(Biomes.BADLANDS);
    private static final int WOODED_BADLANDS_PLATEAU = Registry.BIOME.getId(Biomes.WOODED_BADLANDS_PLATEAU);
    private static final int BADLANDS_PLATEAU = Registry.BIOME.getId(Biomes.BADLANDS_PLATEAU);
    private static final int ERODED_BADLANDS = Registry.BIOME.getId(Biomes.ERODED_BADLANDS);
    private static final int MODIFIED_WOODED_BADLANDS_PLATEAU = Registry.BIOME.getId(Biomes.MODIFIED_WOODED_BADLANDS_PLATEAU);
    private static final int MODIFIED_BADLANDS_PLATEAU = Registry.BIOME.getId(Biomes.MODIFIED_BADLANDS_PLATEAU);
    private static final int MUSHROOM_FIELDS = Registry.BIOME.getId(Biomes.MUSHROOM_FIELDS);
    private static final int MUSHROOM_FIELD_SHORE = Registry.BIOME.getId(Biomes.MUSHROOM_FIELD_SHORE);
    private static final int RIVER = Registry.BIOME.getId(Biomes.RIVER);
    private static final int MOUNTAIN_EDGE = Registry.BIOME.getId(Biomes.MOUNTAIN_EDGE);
    private static final int STONE_SHORE = Registry.BIOME.getId(Biomes.STONE_SHORE);
    private static final int SWAMP = Registry.BIOME.getId(Biomes.SWAMP);
    private static final int TAIGA = Registry.BIOME.getId(Biomes.TAIGA);

    @Override
    public int apply(Context param0, int param1, int param2, int param3, int param4, int param5) {
        Biome var0 = Registry.BIOME.byId(param5);
        if (param5 == MUSHROOM_FIELDS) {
            if (Layers.isShallowOcean(param1) || Layers.isShallowOcean(param2) || Layers.isShallowOcean(param3) || Layers.isShallowOcean(param4)) {
                return MUSHROOM_FIELD_SHORE;
            }
        } else if (var0 != null && var0.getBiomeCategory() == Biome.BiomeCategory.JUNGLE) {
            if (!isJungleCompatible(param1) || !isJungleCompatible(param2) || !isJungleCompatible(param3) || !isJungleCompatible(param4)) {
                return JUNGLE_EDGE;
            }

            if (Layers.isOcean(param1) || Layers.isOcean(param2) || Layers.isOcean(param3) || Layers.isOcean(param4)) {
                return BEACH;
            }
        } else if (param5 != MOUNTAINS && param5 != WOODED_MOUNTAINS && param5 != MOUNTAIN_EDGE) {
            if (var0 != null && var0.getPrecipitation() == Biome.Precipitation.SNOW) {
                if (!Layers.isOcean(param5) && (Layers.isOcean(param1) || Layers.isOcean(param2) || Layers.isOcean(param3) || Layers.isOcean(param4))) {
                    return SNOWY_BEACH;
                }
            } else if (param5 != BADLANDS && param5 != WOODED_BADLANDS_PLATEAU) {
                if (!Layers.isOcean(param5)
                    && param5 != RIVER
                    && param5 != SWAMP
                    && (Layers.isOcean(param1) || Layers.isOcean(param2) || Layers.isOcean(param3) || Layers.isOcean(param4))) {
                    return BEACH;
                }
            } else if (!Layers.isOcean(param1)
                && !Layers.isOcean(param2)
                && !Layers.isOcean(param3)
                && !Layers.isOcean(param4)
                && (!this.isMesa(param1) || !this.isMesa(param2) || !this.isMesa(param3) || !this.isMesa(param4))) {
                return DESERT;
            }
        } else if (!Layers.isOcean(param5) && (Layers.isOcean(param1) || Layers.isOcean(param2) || Layers.isOcean(param3) || Layers.isOcean(param4))) {
            return STONE_SHORE;
        }

        return param5;
    }

    private static boolean isJungleCompatible(int param0) {
        if (Registry.BIOME.byId(param0) != null && Registry.BIOME.byId(param0).getBiomeCategory() == Biome.BiomeCategory.JUNGLE) {
            return true;
        } else {
            return param0 == JUNGLE_EDGE || param0 == JUNGLE || param0 == JUNGLE_HILLS || param0 == FOREST || param0 == TAIGA || Layers.isOcean(param0);
        }
    }

    private boolean isMesa(int param0) {
        return param0 == BADLANDS
            || param0 == WOODED_BADLANDS_PLATEAU
            || param0 == BADLANDS_PLATEAU
            || param0 == ERODED_BADLANDS
            || param0 == MODIFIED_WOODED_BADLANDS_PLATEAU
            || param0 == MODIFIED_BADLANDS_PLATEAU;
    }
}

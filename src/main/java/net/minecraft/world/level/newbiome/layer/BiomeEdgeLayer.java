package net.minecraft.world.level.newbiome.layer;

import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.CastleTransformer;

public enum BiomeEdgeLayer implements CastleTransformer {
    INSTANCE;

    private static final int DESERT = BuiltinRegistries.BIOME.getId(Biomes.DESERT);
    private static final int MOUNTAINS = BuiltinRegistries.BIOME.getId(Biomes.MOUNTAINS);
    private static final int WOODED_MOUNTAINS = BuiltinRegistries.BIOME.getId(Biomes.WOODED_MOUNTAINS);
    private static final int SNOWY_TUNDRA = BuiltinRegistries.BIOME.getId(Biomes.SNOWY_TUNDRA);
    private static final int JUNGLE = BuiltinRegistries.BIOME.getId(Biomes.JUNGLE);
    private static final int BAMBOO_JUNGLE = BuiltinRegistries.BIOME.getId(Biomes.BAMBOO_JUNGLE);
    private static final int JUNGLE_EDGE = BuiltinRegistries.BIOME.getId(Biomes.JUNGLE_EDGE);
    private static final int BADLANDS = BuiltinRegistries.BIOME.getId(Biomes.BADLANDS);
    private static final int BADLANDS_PLATEAU = BuiltinRegistries.BIOME.getId(Biomes.BADLANDS_PLATEAU);
    private static final int WOODED_BADLANDS_PLATEAU = BuiltinRegistries.BIOME.getId(Biomes.WOODED_BADLANDS_PLATEAU);
    private static final int PLAINS = BuiltinRegistries.BIOME.getId(Biomes.PLAINS);
    private static final int GIANT_TREE_TAIGA = BuiltinRegistries.BIOME.getId(Biomes.GIANT_TREE_TAIGA);
    private static final int MOUNTAIN_EDGE = BuiltinRegistries.BIOME.getId(Biomes.MOUNTAIN_EDGE);
    private static final int SWAMP = BuiltinRegistries.BIOME.getId(Biomes.SWAMP);
    private static final int TAIGA = BuiltinRegistries.BIOME.getId(Biomes.TAIGA);
    private static final int SNOWY_TAIGA = BuiltinRegistries.BIOME.getId(Biomes.SNOWY_TAIGA);

    @Override
    public int apply(Context param0, int param1, int param2, int param3, int param4, int param5) {
        int[] var0 = new int[1];
        if (!this.checkEdge(var0, param1, param2, param3, param4, param5, MOUNTAINS, MOUNTAIN_EDGE)
            && !this.checkEdgeStrict(var0, param1, param2, param3, param4, param5, WOODED_BADLANDS_PLATEAU, BADLANDS)
            && !this.checkEdgeStrict(var0, param1, param2, param3, param4, param5, BADLANDS_PLATEAU, BADLANDS)
            && !this.checkEdgeStrict(var0, param1, param2, param3, param4, param5, GIANT_TREE_TAIGA, TAIGA)) {
            if (param5 != DESERT || param1 != SNOWY_TUNDRA && param2 != SNOWY_TUNDRA && param4 != SNOWY_TUNDRA && param3 != SNOWY_TUNDRA) {
                if (param5 == SWAMP) {
                    if (param1 == DESERT
                        || param2 == DESERT
                        || param4 == DESERT
                        || param3 == DESERT
                        || param1 == SNOWY_TAIGA
                        || param2 == SNOWY_TAIGA
                        || param4 == SNOWY_TAIGA
                        || param3 == SNOWY_TAIGA
                        || param1 == SNOWY_TUNDRA
                        || param2 == SNOWY_TUNDRA
                        || param4 == SNOWY_TUNDRA
                        || param3 == SNOWY_TUNDRA) {
                        return PLAINS;
                    }

                    if (param1 == JUNGLE
                        || param3 == JUNGLE
                        || param2 == JUNGLE
                        || param4 == JUNGLE
                        || param1 == BAMBOO_JUNGLE
                        || param3 == BAMBOO_JUNGLE
                        || param2 == BAMBOO_JUNGLE
                        || param4 == BAMBOO_JUNGLE) {
                        return JUNGLE_EDGE;
                    }
                }

                return param5;
            } else {
                return WOODED_MOUNTAINS;
            }
        } else {
            return var0[0];
        }
    }

    private boolean checkEdge(int[] param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7) {
        if (!Layers.isSame(param5, param6)) {
            return false;
        } else {
            if (this.isValidTemperatureEdge(param1, param6)
                && this.isValidTemperatureEdge(param2, param6)
                && this.isValidTemperatureEdge(param4, param6)
                && this.isValidTemperatureEdge(param3, param6)) {
                param0[0] = param5;
            } else {
                param0[0] = param7;
            }

            return true;
        }
    }

    private boolean checkEdgeStrict(int[] param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7) {
        if (param5 != param6) {
            return false;
        } else {
            if (Layers.isSame(param1, param6) && Layers.isSame(param2, param6) && Layers.isSame(param4, param6) && Layers.isSame(param3, param6)) {
                param0[0] = param5;
            } else {
                param0[0] = param7;
            }

            return true;
        }
    }

    private boolean isValidTemperatureEdge(int param0, int param1) {
        if (Layers.isSame(param0, param1)) {
            return true;
        } else {
            Biome var0 = BuiltinRegistries.BIOME.byId(param0);
            Biome var1 = BuiltinRegistries.BIOME.byId(param1);
            if (var0 != null && var1 != null) {
                Biome.BiomeTempCategory var2 = var0.getTemperatureCategory();
                Biome.BiomeTempCategory var3 = var1.getTemperatureCategory();
                return var2 == var3 || var2 == Biome.BiomeTempCategory.MEDIUM || var3 == Biome.BiomeTempCategory.MEDIUM;
            } else {
                return false;
            }
        }
    }
}

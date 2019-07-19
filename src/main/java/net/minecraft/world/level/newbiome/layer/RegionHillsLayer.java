package net.minecraft.world.level.newbiome.layer;

import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer2;
import net.minecraft.world.level.newbiome.layer.traits.DimensionOffset1Transformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public enum RegionHillsLayer implements AreaTransformer2, DimensionOffset1Transformer {
    INSTANCE;

    private static final Logger LOGGER = LogManager.getLogger();
    private static final int BIRCH_FOREST = Registry.BIOME.getId(Biomes.BIRCH_FOREST);
    private static final int BIRCH_FOREST_HILLS = Registry.BIOME.getId(Biomes.BIRCH_FOREST_HILLS);
    private static final int DESERT = Registry.BIOME.getId(Biomes.DESERT);
    private static final int DESERT_HILLS = Registry.BIOME.getId(Biomes.DESERT_HILLS);
    private static final int MOUNTAINS = Registry.BIOME.getId(Biomes.MOUNTAINS);
    private static final int WOODED_MOUNTAINS = Registry.BIOME.getId(Biomes.WOODED_MOUNTAINS);
    private static final int FOREST = Registry.BIOME.getId(Biomes.FOREST);
    private static final int WOODED_HILLS = Registry.BIOME.getId(Biomes.WOODED_HILLS);
    private static final int SNOWY_TUNDRA = Registry.BIOME.getId(Biomes.SNOWY_TUNDRA);
    private static final int SNOWY_MOUNTAIN = Registry.BIOME.getId(Biomes.SNOWY_MOUNTAINS);
    private static final int JUNGLE = Registry.BIOME.getId(Biomes.JUNGLE);
    private static final int JUNGLE_HILLS = Registry.BIOME.getId(Biomes.JUNGLE_HILLS);
    private static final int BAMBOO_JUNGLE = Registry.BIOME.getId(Biomes.BAMBOO_JUNGLE);
    private static final int BAMBOO_JUNGLE_HILLS = Registry.BIOME.getId(Biomes.BAMBOO_JUNGLE_HILLS);
    private static final int BADLANDS = Registry.BIOME.getId(Biomes.BADLANDS);
    private static final int WOODED_BADLANDS_PLATEAU = Registry.BIOME.getId(Biomes.WOODED_BADLANDS_PLATEAU);
    private static final int PLAINS = Registry.BIOME.getId(Biomes.PLAINS);
    private static final int GIANT_TREE_TAIGA = Registry.BIOME.getId(Biomes.GIANT_TREE_TAIGA);
    private static final int GIANT_TREE_TAIGA_HILLS = Registry.BIOME.getId(Biomes.GIANT_TREE_TAIGA_HILLS);
    private static final int DARK_FOREST = Registry.BIOME.getId(Biomes.DARK_FOREST);
    private static final int SAVANNA = Registry.BIOME.getId(Biomes.SAVANNA);
    private static final int SAVANNA_PLATEAU = Registry.BIOME.getId(Biomes.SAVANNA_PLATEAU);
    private static final int TAIGA = Registry.BIOME.getId(Biomes.TAIGA);
    private static final int SNOWY_TAIGA = Registry.BIOME.getId(Biomes.SNOWY_TAIGA);
    private static final int SNOWY_TAIGA_HILLS = Registry.BIOME.getId(Biomes.SNOWY_TAIGA_HILLS);
    private static final int TAIGA_HILLS = Registry.BIOME.getId(Biomes.TAIGA_HILLS);

    @Override
    public int applyPixel(Context param0, Area param1, Area param2, int param3, int param4) {
        int var0 = param1.get(this.getParentX(param3 + 1), this.getParentY(param4 + 1));
        int var1 = param2.get(this.getParentX(param3 + 1), this.getParentY(param4 + 1));
        if (var0 > 255) {
            LOGGER.debug("old! {}", var0);
        }

        int var2 = (var1 - 2) % 29;
        if (!Layers.isShallowOcean(var0) && var1 >= 2 && var2 == 1) {
            Biome var3 = Registry.BIOME.byId(var0);
            if (var3 == null || !var3.isMutated()) {
                Biome var4 = Biome.getMutatedVariant(var3);
                return var4 == null ? var0 : Registry.BIOME.getId(var4);
            }
        }

        if (param0.nextRandom(3) == 0 || var2 == 0) {
            int var5 = var0;
            if (var0 == DESERT) {
                var5 = DESERT_HILLS;
            } else if (var0 == FOREST) {
                var5 = WOODED_HILLS;
            } else if (var0 == BIRCH_FOREST) {
                var5 = BIRCH_FOREST_HILLS;
            } else if (var0 == DARK_FOREST) {
                var5 = PLAINS;
            } else if (var0 == TAIGA) {
                var5 = TAIGA_HILLS;
            } else if (var0 == GIANT_TREE_TAIGA) {
                var5 = GIANT_TREE_TAIGA_HILLS;
            } else if (var0 == SNOWY_TAIGA) {
                var5 = SNOWY_TAIGA_HILLS;
            } else if (var0 == PLAINS) {
                var5 = param0.nextRandom(3) == 0 ? WOODED_HILLS : FOREST;
            } else if (var0 == SNOWY_TUNDRA) {
                var5 = SNOWY_MOUNTAIN;
            } else if (var0 == JUNGLE) {
                var5 = JUNGLE_HILLS;
            } else if (var0 == BAMBOO_JUNGLE) {
                var5 = BAMBOO_JUNGLE_HILLS;
            } else if (var0 == Layers.OCEAN) {
                var5 = Layers.DEEP_OCEAN;
            } else if (var0 == Layers.LUKEWARM_OCEAN) {
                var5 = Layers.DEEP_LUKEWARM_OCEAN;
            } else if (var0 == Layers.COLD_OCEAN) {
                var5 = Layers.DEEP_COLD_OCEAN;
            } else if (var0 == Layers.FROZEN_OCEAN) {
                var5 = Layers.DEEP_FROZEN_OCEAN;
            } else if (var0 == MOUNTAINS) {
                var5 = WOODED_MOUNTAINS;
            } else if (var0 == SAVANNA) {
                var5 = SAVANNA_PLATEAU;
            } else if (Layers.isSame(var0, WOODED_BADLANDS_PLATEAU)) {
                var5 = BADLANDS;
            } else if ((var0 == Layers.DEEP_OCEAN || var0 == Layers.DEEP_LUKEWARM_OCEAN || var0 == Layers.DEEP_COLD_OCEAN || var0 == Layers.DEEP_FROZEN_OCEAN)
                && param0.nextRandom(3) == 0) {
                var5 = param0.nextRandom(2) == 0 ? PLAINS : FOREST;
            }

            if (var2 == 0 && var5 != var0) {
                Biome var6 = Biome.getMutatedVariant(Registry.BIOME.byId(var5));
                var5 = var6 == null ? var0 : Registry.BIOME.getId(var6);
            }

            if (var5 != var0) {
                int var7 = 0;
                if (Layers.isSame(param1.get(this.getParentX(param3 + 1), this.getParentY(param4 + 0)), var0)) {
                    ++var7;
                }

                if (Layers.isSame(param1.get(this.getParentX(param3 + 2), this.getParentY(param4 + 1)), var0)) {
                    ++var7;
                }

                if (Layers.isSame(param1.get(this.getParentX(param3 + 0), this.getParentY(param4 + 1)), var0)) {
                    ++var7;
                }

                if (Layers.isSame(param1.get(this.getParentX(param3 + 1), this.getParentY(param4 + 2)), var0)) {
                    ++var7;
                }

                if (var7 >= 3) {
                    return var5;
                }
            }
        }

        return var0;
    }
}

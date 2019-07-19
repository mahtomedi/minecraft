package net.minecraft.world.level.newbiome.layer;

import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelType;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.OverworldGeneratorSettings;
import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.C0Transformer;

public class BiomeInitLayer implements C0Transformer {
    private static final int BIRCH_FOREST = Registry.BIOME.getId(Biomes.BIRCH_FOREST);
    private static final int DESERT = Registry.BIOME.getId(Biomes.DESERT);
    private static final int MOUNTAINS = Registry.BIOME.getId(Biomes.MOUNTAINS);
    private static final int FOREST = Registry.BIOME.getId(Biomes.FOREST);
    private static final int SNOWY_TUNDRA = Registry.BIOME.getId(Biomes.SNOWY_TUNDRA);
    private static final int JUNGLE = Registry.BIOME.getId(Biomes.JUNGLE);
    private static final int BADLANDS_PLATEAU = Registry.BIOME.getId(Biomes.BADLANDS_PLATEAU);
    private static final int WOODED_BADLANDS_PLATEAU = Registry.BIOME.getId(Biomes.WOODED_BADLANDS_PLATEAU);
    private static final int MUSHROOM_FIELDS = Registry.BIOME.getId(Biomes.MUSHROOM_FIELDS);
    private static final int PLAINS = Registry.BIOME.getId(Biomes.PLAINS);
    private static final int GIANT_TREE_TAIGA = Registry.BIOME.getId(Biomes.GIANT_TREE_TAIGA);
    private static final int DARK_FOREST = Registry.BIOME.getId(Biomes.DARK_FOREST);
    private static final int SAVANNA = Registry.BIOME.getId(Biomes.SAVANNA);
    private static final int SWAMP = Registry.BIOME.getId(Biomes.SWAMP);
    private static final int TAIGA = Registry.BIOME.getId(Biomes.TAIGA);
    private static final int SNOWY_TAIGA = Registry.BIOME.getId(Biomes.SNOWY_TAIGA);
    private static final int[] LEGACY_WARM_BIOMES = new int[]{DESERT, FOREST, MOUNTAINS, SWAMP, PLAINS, TAIGA};
    private static final int[] WARM_BIOMES = new int[]{DESERT, DESERT, DESERT, SAVANNA, SAVANNA, PLAINS};
    private static final int[] MEDIUM_BIOMES = new int[]{FOREST, DARK_FOREST, MOUNTAINS, PLAINS, BIRCH_FOREST, SWAMP};
    private static final int[] COLD_BIOMES = new int[]{FOREST, MOUNTAINS, TAIGA, PLAINS};
    private static final int[] ICE_BIOMES = new int[]{SNOWY_TUNDRA, SNOWY_TUNDRA, SNOWY_TUNDRA, SNOWY_TAIGA};
    private final OverworldGeneratorSettings settings;
    private int[] warmBiomes = WARM_BIOMES;

    public BiomeInitLayer(LevelType param0, OverworldGeneratorSettings param1) {
        if (param0 == LevelType.NORMAL_1_1) {
            this.warmBiomes = LEGACY_WARM_BIOMES;
            this.settings = null;
        } else {
            this.settings = param1;
        }

    }

    @Override
    public int apply(Context param0, int param1) {
        if (this.settings != null && this.settings.getFixedBiome() >= 0) {
            return this.settings.getFixedBiome();
        } else {
            int var0 = (param1 & 3840) >> 8;
            param1 &= -3841;
            if (!Layers.isOcean(param1) && param1 != MUSHROOM_FIELDS) {
                switch(param1) {
                    case 1:
                        if (var0 > 0) {
                            return param0.nextRandom(3) == 0 ? BADLANDS_PLATEAU : WOODED_BADLANDS_PLATEAU;
                        }

                        return this.warmBiomes[param0.nextRandom(this.warmBiomes.length)];
                    case 2:
                        if (var0 > 0) {
                            return JUNGLE;
                        }

                        return MEDIUM_BIOMES[param0.nextRandom(MEDIUM_BIOMES.length)];
                    case 3:
                        if (var0 > 0) {
                            return GIANT_TREE_TAIGA;
                        }

                        return COLD_BIOMES[param0.nextRandom(COLD_BIOMES.length)];
                    case 4:
                        return ICE_BIOMES[param0.nextRandom(ICE_BIOMES.length)];
                    default:
                        return MUSHROOM_FIELDS;
                }
            } else {
                return param1;
            }
        }
    }
}

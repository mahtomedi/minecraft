package net.minecraft.data.worldgen.biome;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public abstract class BiomeData {
    public static void bootstrap(BootstapContext<Biome> param0) {
        HolderGetter<PlacedFeature> var0 = param0.lookup(Registries.PLACED_FEATURE);
        HolderGetter<ConfiguredWorldCarver<?>> var1 = param0.lookup(Registries.CONFIGURED_CARVER);
        param0.register(Biomes.THE_VOID, OverworldBiomes.theVoid(var0, var1));
        param0.register(Biomes.PLAINS, OverworldBiomes.plains(var0, var1, false, false, false));
        param0.register(Biomes.SUNFLOWER_PLAINS, OverworldBiomes.plains(var0, var1, true, false, false));
        param0.register(Biomes.SNOWY_PLAINS, OverworldBiomes.plains(var0, var1, false, true, false));
        param0.register(Biomes.ICE_SPIKES, OverworldBiomes.plains(var0, var1, false, true, true));
        param0.register(Biomes.DESERT, OverworldBiomes.desert(var0, var1));
        param0.register(Biomes.SWAMP, OverworldBiomes.swamp(var0, var1));
        param0.register(Biomes.MANGROVE_SWAMP, OverworldBiomes.mangroveSwamp(var0, var1));
        param0.register(Biomes.FOREST, OverworldBiomes.forest(var0, var1, false, false, false));
        param0.register(Biomes.FLOWER_FOREST, OverworldBiomes.forest(var0, var1, false, false, true));
        param0.register(Biomes.BIRCH_FOREST, OverworldBiomes.forest(var0, var1, true, false, false));
        param0.register(Biomes.DARK_FOREST, OverworldBiomes.darkForest(var0, var1));
        param0.register(Biomes.OLD_GROWTH_BIRCH_FOREST, OverworldBiomes.forest(var0, var1, true, true, false));
        param0.register(Biomes.OLD_GROWTH_PINE_TAIGA, OverworldBiomes.oldGrowthTaiga(var0, var1, false));
        param0.register(Biomes.OLD_GROWTH_SPRUCE_TAIGA, OverworldBiomes.oldGrowthTaiga(var0, var1, true));
        param0.register(Biomes.TAIGA, OverworldBiomes.taiga(var0, var1, false));
        param0.register(Biomes.SNOWY_TAIGA, OverworldBiomes.taiga(var0, var1, true));
        param0.register(Biomes.SAVANNA, OverworldBiomes.savanna(var0, var1, false, false));
        param0.register(Biomes.SAVANNA_PLATEAU, OverworldBiomes.savanna(var0, var1, false, true));
        param0.register(Biomes.WINDSWEPT_HILLS, OverworldBiomes.windsweptHills(var0, var1, false));
        param0.register(Biomes.WINDSWEPT_GRAVELLY_HILLS, OverworldBiomes.windsweptHills(var0, var1, false));
        param0.register(Biomes.WINDSWEPT_FOREST, OverworldBiomes.windsweptHills(var0, var1, true));
        param0.register(Biomes.WINDSWEPT_SAVANNA, OverworldBiomes.savanna(var0, var1, true, false));
        param0.register(Biomes.JUNGLE, OverworldBiomes.jungle(var0, var1));
        param0.register(Biomes.SPARSE_JUNGLE, OverworldBiomes.sparseJungle(var0, var1));
        param0.register(Biomes.BAMBOO_JUNGLE, OverworldBiomes.bambooJungle(var0, var1));
        param0.register(Biomes.BADLANDS, OverworldBiomes.badlands(var0, var1, false));
        param0.register(Biomes.ERODED_BADLANDS, OverworldBiomes.badlands(var0, var1, false));
        param0.register(Biomes.WOODED_BADLANDS, OverworldBiomes.badlands(var0, var1, true));
        param0.register(Biomes.MEADOW, OverworldBiomes.meadowOrCherryGrove(var0, var1, false));
        param0.register(Biomes.GROVE, OverworldBiomes.grove(var0, var1));
        param0.register(Biomes.SNOWY_SLOPES, OverworldBiomes.snowySlopes(var0, var1));
        param0.register(Biomes.FROZEN_PEAKS, OverworldBiomes.frozenPeaks(var0, var1));
        param0.register(Biomes.JAGGED_PEAKS, OverworldBiomes.jaggedPeaks(var0, var1));
        param0.register(Biomes.STONY_PEAKS, OverworldBiomes.stonyPeaks(var0, var1));
        param0.register(Biomes.RIVER, OverworldBiomes.river(var0, var1, false));
        param0.register(Biomes.FROZEN_RIVER, OverworldBiomes.river(var0, var1, true));
        param0.register(Biomes.BEACH, OverworldBiomes.beach(var0, var1, false, false));
        param0.register(Biomes.SNOWY_BEACH, OverworldBiomes.beach(var0, var1, true, false));
        param0.register(Biomes.STONY_SHORE, OverworldBiomes.beach(var0, var1, false, true));
        param0.register(Biomes.WARM_OCEAN, OverworldBiomes.warmOcean(var0, var1));
        param0.register(Biomes.LUKEWARM_OCEAN, OverworldBiomes.lukeWarmOcean(var0, var1, false));
        param0.register(Biomes.DEEP_LUKEWARM_OCEAN, OverworldBiomes.lukeWarmOcean(var0, var1, true));
        param0.register(Biomes.OCEAN, OverworldBiomes.ocean(var0, var1, false));
        param0.register(Biomes.DEEP_OCEAN, OverworldBiomes.ocean(var0, var1, true));
        param0.register(Biomes.COLD_OCEAN, OverworldBiomes.coldOcean(var0, var1, false));
        param0.register(Biomes.DEEP_COLD_OCEAN, OverworldBiomes.coldOcean(var0, var1, true));
        param0.register(Biomes.FROZEN_OCEAN, OverworldBiomes.frozenOcean(var0, var1, false));
        param0.register(Biomes.DEEP_FROZEN_OCEAN, OverworldBiomes.frozenOcean(var0, var1, true));
        param0.register(Biomes.MUSHROOM_FIELDS, OverworldBiomes.mushroomFields(var0, var1));
        param0.register(Biomes.DRIPSTONE_CAVES, OverworldBiomes.dripstoneCaves(var0, var1));
        param0.register(Biomes.LUSH_CAVES, OverworldBiomes.lushCaves(var0, var1));
        param0.register(Biomes.DEEP_DARK, OverworldBiomes.deepDark(var0, var1));
        param0.register(Biomes.NETHER_WASTES, NetherBiomes.netherWastes(var0, var1));
        param0.register(Biomes.WARPED_FOREST, NetherBiomes.warpedForest(var0, var1));
        param0.register(Biomes.CRIMSON_FOREST, NetherBiomes.crimsonForest(var0, var1));
        param0.register(Biomes.SOUL_SAND_VALLEY, NetherBiomes.soulSandValley(var0, var1));
        param0.register(Biomes.BASALT_DELTAS, NetherBiomes.basaltDeltas(var0, var1));
        param0.register(Biomes.THE_END, EndBiomes.theEnd(var0, var1));
        param0.register(Biomes.END_HIGHLANDS, EndBiomes.endHighlands(var0, var1));
        param0.register(Biomes.END_MIDLANDS, EndBiomes.endMidlands(var0, var1));
        param0.register(Biomes.SMALL_END_ISLANDS, EndBiomes.smallEndIslands(var0, var1));
        param0.register(Biomes.END_BARRENS, EndBiomes.endBarrens(var0, var1));
    }

    public static void nextUpdate(BootstapContext<Biome> param0) {
        HolderGetter<PlacedFeature> var0 = param0.lookup(Registries.PLACED_FEATURE);
        HolderGetter<ConfiguredWorldCarver<?>> var1 = param0.lookup(Registries.CONFIGURED_CARVER);
        param0.register(Biomes.CHERRY_GROVE, OverworldBiomes.meadowOrCherryGrove(var0, var1, true));
    }
}

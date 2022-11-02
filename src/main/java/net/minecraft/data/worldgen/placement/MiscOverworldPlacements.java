package net.minecraft.data.worldgen.placement;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.features.MiscOverworldFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.heightproviders.VeryBiasedToBottomHeight;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.EnvironmentScanPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RandomOffsetPlacement;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.placement.SurfaceRelativeThresholdFilter;
import net.minecraft.world.level.material.Fluids;

public class MiscOverworldPlacements {
    public static final ResourceKey<PlacedFeature> ICE_SPIKE = PlacementUtils.createKey("ice_spike");
    public static final ResourceKey<PlacedFeature> ICE_PATCH = PlacementUtils.createKey("ice_patch");
    public static final ResourceKey<PlacedFeature> FOREST_ROCK = PlacementUtils.createKey("forest_rock");
    public static final ResourceKey<PlacedFeature> ICEBERG_PACKED = PlacementUtils.createKey("iceberg_packed");
    public static final ResourceKey<PlacedFeature> ICEBERG_BLUE = PlacementUtils.createKey("iceberg_blue");
    public static final ResourceKey<PlacedFeature> BLUE_ICE = PlacementUtils.createKey("blue_ice");
    public static final ResourceKey<PlacedFeature> LAKE_LAVA_UNDERGROUND = PlacementUtils.createKey("lake_lava_underground");
    public static final ResourceKey<PlacedFeature> LAKE_LAVA_SURFACE = PlacementUtils.createKey("lake_lava_surface");
    public static final ResourceKey<PlacedFeature> DISK_CLAY = PlacementUtils.createKey("disk_clay");
    public static final ResourceKey<PlacedFeature> DISK_GRAVEL = PlacementUtils.createKey("disk_gravel");
    public static final ResourceKey<PlacedFeature> DISK_SAND = PlacementUtils.createKey("disk_sand");
    public static final ResourceKey<PlacedFeature> DISK_GRASS = PlacementUtils.createKey("disk_grass");
    public static final ResourceKey<PlacedFeature> FREEZE_TOP_LAYER = PlacementUtils.createKey("freeze_top_layer");
    public static final ResourceKey<PlacedFeature> VOID_START_PLATFORM = PlacementUtils.createKey("void_start_platform");
    public static final ResourceKey<PlacedFeature> DESERT_WELL = PlacementUtils.createKey("desert_well");
    public static final ResourceKey<PlacedFeature> SPRING_LAVA = PlacementUtils.createKey("spring_lava");
    public static final ResourceKey<PlacedFeature> SPRING_LAVA_FROZEN = PlacementUtils.createKey("spring_lava_frozen");
    public static final ResourceKey<PlacedFeature> SPRING_WATER = PlacementUtils.createKey("spring_water");

    public static void bootstrap(BootstapContext<PlacedFeature> param0) {
        HolderGetter<ConfiguredFeature<?, ?>> var0 = param0.lookup(Registry.CONFIGURED_FEATURE_REGISTRY);
        Holder<ConfiguredFeature<?, ?>> var1 = var0.getOrThrow(MiscOverworldFeatures.ICE_SPIKE);
        Holder<ConfiguredFeature<?, ?>> var2 = var0.getOrThrow(MiscOverworldFeatures.ICE_PATCH);
        Holder<ConfiguredFeature<?, ?>> var3 = var0.getOrThrow(MiscOverworldFeatures.FOREST_ROCK);
        Holder<ConfiguredFeature<?, ?>> var4 = var0.getOrThrow(MiscOverworldFeatures.ICEBERG_PACKED);
        Holder<ConfiguredFeature<?, ?>> var5 = var0.getOrThrow(MiscOverworldFeatures.ICEBERG_BLUE);
        Holder<ConfiguredFeature<?, ?>> var6 = var0.getOrThrow(MiscOverworldFeatures.BLUE_ICE);
        Holder<ConfiguredFeature<?, ?>> var7 = var0.getOrThrow(MiscOverworldFeatures.LAKE_LAVA);
        Holder<ConfiguredFeature<?, ?>> var8 = var0.getOrThrow(MiscOverworldFeatures.DISK_CLAY);
        Holder<ConfiguredFeature<?, ?>> var9 = var0.getOrThrow(MiscOverworldFeatures.DISK_GRAVEL);
        Holder<ConfiguredFeature<?, ?>> var10 = var0.getOrThrow(MiscOverworldFeatures.DISK_SAND);
        Holder<ConfiguredFeature<?, ?>> var11 = var0.getOrThrow(MiscOverworldFeatures.DISK_GRASS);
        Holder<ConfiguredFeature<?, ?>> var12 = var0.getOrThrow(MiscOverworldFeatures.FREEZE_TOP_LAYER);
        Holder<ConfiguredFeature<?, ?>> var13 = var0.getOrThrow(MiscOverworldFeatures.VOID_START_PLATFORM);
        Holder<ConfiguredFeature<?, ?>> var14 = var0.getOrThrow(MiscOverworldFeatures.DESERT_WELL);
        Holder<ConfiguredFeature<?, ?>> var15 = var0.getOrThrow(MiscOverworldFeatures.SPRING_LAVA_OVERWORLD);
        Holder<ConfiguredFeature<?, ?>> var16 = var0.getOrThrow(MiscOverworldFeatures.SPRING_LAVA_FROZEN);
        Holder<ConfiguredFeature<?, ?>> var17 = var0.getOrThrow(MiscOverworldFeatures.SPRING_WATER);
        PlacementUtils.register(param0, ICE_SPIKE, var1, CountPlacement.of(3), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(
            param0,
            ICE_PATCH,
            var2,
            CountPlacement.of(2),
            InSquarePlacement.spread(),
            PlacementUtils.HEIGHTMAP,
            RandomOffsetPlacement.vertical(ConstantInt.of(-1)),
            BlockPredicateFilter.forPredicate(BlockPredicate.matchesBlocks(Blocks.SNOW_BLOCK)),
            BiomeFilter.biome()
        );
        PlacementUtils.register(param0, FOREST_ROCK, var3, CountPlacement.of(2), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(param0, ICEBERG_BLUE, var5, RarityFilter.onAverageOnceEvery(200), InSquarePlacement.spread(), BiomeFilter.biome());
        PlacementUtils.register(param0, ICEBERG_PACKED, var4, RarityFilter.onAverageOnceEvery(16), InSquarePlacement.spread(), BiomeFilter.biome());
        PlacementUtils.register(
            param0,
            BLUE_ICE,
            var6,
            CountPlacement.of(UniformInt.of(0, 19)),
            InSquarePlacement.spread(),
            HeightRangePlacement.uniform(VerticalAnchor.absolute(30), VerticalAnchor.absolute(61)),
            BiomeFilter.biome()
        );
        PlacementUtils.register(
            param0,
            LAKE_LAVA_UNDERGROUND,
            var7,
            RarityFilter.onAverageOnceEvery(9),
            InSquarePlacement.spread(),
            HeightRangePlacement.of(UniformHeight.of(VerticalAnchor.absolute(0), VerticalAnchor.top())),
            EnvironmentScanPlacement.scanningFor(
                Direction.DOWN,
                BlockPredicate.allOf(BlockPredicate.not(BlockPredicate.ONLY_IN_AIR_PREDICATE), BlockPredicate.insideWorld(new BlockPos(0, -5, 0))),
                32
            ),
            SurfaceRelativeThresholdFilter.of(Heightmap.Types.OCEAN_FLOOR_WG, Integer.MIN_VALUE, -5),
            BiomeFilter.biome()
        );
        PlacementUtils.register(
            param0,
            LAKE_LAVA_SURFACE,
            var7,
            RarityFilter.onAverageOnceEvery(200),
            InSquarePlacement.spread(),
            PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
            BiomeFilter.biome()
        );
        PlacementUtils.register(
            param0,
            DISK_CLAY,
            var8,
            InSquarePlacement.spread(),
            PlacementUtils.HEIGHTMAP_TOP_SOLID,
            BlockPredicateFilter.forPredicate(BlockPredicate.matchesFluids(Fluids.WATER)),
            BiomeFilter.biome()
        );
        PlacementUtils.register(
            param0,
            DISK_GRAVEL,
            var9,
            InSquarePlacement.spread(),
            PlacementUtils.HEIGHTMAP_TOP_SOLID,
            BlockPredicateFilter.forPredicate(BlockPredicate.matchesFluids(Fluids.WATER)),
            BiomeFilter.biome()
        );
        PlacementUtils.register(
            param0,
            DISK_SAND,
            var10,
            CountPlacement.of(3),
            InSquarePlacement.spread(),
            PlacementUtils.HEIGHTMAP_TOP_SOLID,
            BlockPredicateFilter.forPredicate(BlockPredicate.matchesFluids(Fluids.WATER)),
            BiomeFilter.biome()
        );
        PlacementUtils.register(
            param0,
            DISK_GRASS,
            var11,
            CountPlacement.of(1),
            InSquarePlacement.spread(),
            PlacementUtils.HEIGHTMAP_TOP_SOLID,
            RandomOffsetPlacement.vertical(ConstantInt.of(-1)),
            BlockPredicateFilter.forPredicate(BlockPredicate.matchesBlocks(Blocks.MUD)),
            BiomeFilter.biome()
        );
        PlacementUtils.register(param0, FREEZE_TOP_LAYER, var12, BiomeFilter.biome());
        PlacementUtils.register(param0, VOID_START_PLATFORM, var13, BiomeFilter.biome());
        PlacementUtils.register(
            param0, DESERT_WELL, var14, RarityFilter.onAverageOnceEvery(1000), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
        );
        PlacementUtils.register(
            param0,
            SPRING_LAVA,
            var15,
            CountPlacement.of(20),
            InSquarePlacement.spread(),
            HeightRangePlacement.of(VeryBiasedToBottomHeight.of(VerticalAnchor.bottom(), VerticalAnchor.belowTop(8), 8)),
            BiomeFilter.biome()
        );
        PlacementUtils.register(
            param0,
            SPRING_LAVA_FROZEN,
            var16,
            CountPlacement.of(20),
            InSquarePlacement.spread(),
            HeightRangePlacement.of(VeryBiasedToBottomHeight.of(VerticalAnchor.bottom(), VerticalAnchor.belowTop(8), 8)),
            BiomeFilter.biome()
        );
        PlacementUtils.register(
            param0,
            SPRING_WATER,
            var17,
            CountPlacement.of(25),
            InSquarePlacement.spread(),
            HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(192)),
            BiomeFilter.biome()
        );
    }
}

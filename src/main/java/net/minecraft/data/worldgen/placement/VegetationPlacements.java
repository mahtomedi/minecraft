package net.minecraft.data.worldgen.placement;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.ClampedInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.NoiseBasedCountPlacement;
import net.minecraft.world.level.levelgen.placement.NoiseThresholdCountPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.placement.SurfaceWaterDepthFilter;

public class VegetationPlacements {
    public static final ResourceKey<PlacedFeature> BAMBOO_LIGHT = PlacementUtils.createKey("bamboo_light");
    public static final ResourceKey<PlacedFeature> BAMBOO = PlacementUtils.createKey("bamboo");
    public static final ResourceKey<PlacedFeature> VINES = PlacementUtils.createKey("vines");
    public static final ResourceKey<PlacedFeature> PATCH_SUNFLOWER = PlacementUtils.createKey("patch_sunflower");
    public static final ResourceKey<PlacedFeature> PATCH_PUMPKIN = PlacementUtils.createKey("patch_pumpkin");
    public static final ResourceKey<PlacedFeature> PATCH_GRASS_PLAIN = PlacementUtils.createKey("patch_grass_plain");
    public static final ResourceKey<PlacedFeature> PATCH_GRASS_FOREST = PlacementUtils.createKey("patch_grass_forest");
    public static final ResourceKey<PlacedFeature> PATCH_GRASS_BADLANDS = PlacementUtils.createKey("patch_grass_badlands");
    public static final ResourceKey<PlacedFeature> PATCH_GRASS_SAVANNA = PlacementUtils.createKey("patch_grass_savanna");
    public static final ResourceKey<PlacedFeature> PATCH_GRASS_NORMAL = PlacementUtils.createKey("patch_grass_normal");
    public static final ResourceKey<PlacedFeature> PATCH_GRASS_TAIGA_2 = PlacementUtils.createKey("patch_grass_taiga_2");
    public static final ResourceKey<PlacedFeature> PATCH_GRASS_TAIGA = PlacementUtils.createKey("patch_grass_taiga");
    public static final ResourceKey<PlacedFeature> PATCH_GRASS_JUNGLE = PlacementUtils.createKey("patch_grass_jungle");
    public static final ResourceKey<PlacedFeature> GRASS_BONEMEAL = PlacementUtils.createKey("grass_bonemeal");
    public static final ResourceKey<PlacedFeature> PATCH_DEAD_BUSH_2 = PlacementUtils.createKey("patch_dead_bush_2");
    public static final ResourceKey<PlacedFeature> PATCH_DEAD_BUSH = PlacementUtils.createKey("patch_dead_bush");
    public static final ResourceKey<PlacedFeature> PATCH_DEAD_BUSH_BADLANDS = PlacementUtils.createKey("patch_dead_bush_badlands");
    public static final ResourceKey<PlacedFeature> PATCH_MELON = PlacementUtils.createKey("patch_melon");
    public static final ResourceKey<PlacedFeature> PATCH_MELON_SPARSE = PlacementUtils.createKey("patch_melon_sparse");
    public static final ResourceKey<PlacedFeature> PATCH_BERRY_COMMON = PlacementUtils.createKey("patch_berry_common");
    public static final ResourceKey<PlacedFeature> PATCH_BERRY_RARE = PlacementUtils.createKey("patch_berry_rare");
    public static final ResourceKey<PlacedFeature> PATCH_WATERLILY = PlacementUtils.createKey("patch_waterlily");
    public static final ResourceKey<PlacedFeature> PATCH_TALL_GRASS_2 = PlacementUtils.createKey("patch_tall_grass_2");
    public static final ResourceKey<PlacedFeature> PATCH_TALL_GRASS = PlacementUtils.createKey("patch_tall_grass");
    public static final ResourceKey<PlacedFeature> PATCH_LARGE_FERN = PlacementUtils.createKey("patch_large_fern");
    public static final ResourceKey<PlacedFeature> PATCH_CACTUS_DESERT = PlacementUtils.createKey("patch_cactus_desert");
    public static final ResourceKey<PlacedFeature> PATCH_CACTUS_DECORATED = PlacementUtils.createKey("patch_cactus_decorated");
    public static final ResourceKey<PlacedFeature> PATCH_SUGAR_CANE_SWAMP = PlacementUtils.createKey("patch_sugar_cane_swamp");
    public static final ResourceKey<PlacedFeature> PATCH_SUGAR_CANE_DESERT = PlacementUtils.createKey("patch_sugar_cane_desert");
    public static final ResourceKey<PlacedFeature> PATCH_SUGAR_CANE_BADLANDS = PlacementUtils.createKey("patch_sugar_cane_badlands");
    public static final ResourceKey<PlacedFeature> PATCH_SUGAR_CANE = PlacementUtils.createKey("patch_sugar_cane");
    public static final ResourceKey<PlacedFeature> BROWN_MUSHROOM_NETHER = PlacementUtils.createKey("brown_mushroom_nether");
    public static final ResourceKey<PlacedFeature> RED_MUSHROOM_NETHER = PlacementUtils.createKey("red_mushroom_nether");
    public static final ResourceKey<PlacedFeature> BROWN_MUSHROOM_NORMAL = PlacementUtils.createKey("brown_mushroom_normal");
    public static final ResourceKey<PlacedFeature> RED_MUSHROOM_NORMAL = PlacementUtils.createKey("red_mushroom_normal");
    public static final ResourceKey<PlacedFeature> BROWN_MUSHROOM_TAIGA = PlacementUtils.createKey("brown_mushroom_taiga");
    public static final ResourceKey<PlacedFeature> RED_MUSHROOM_TAIGA = PlacementUtils.createKey("red_mushroom_taiga");
    public static final ResourceKey<PlacedFeature> BROWN_MUSHROOM_OLD_GROWTH = PlacementUtils.createKey("brown_mushroom_old_growth");
    public static final ResourceKey<PlacedFeature> RED_MUSHROOM_OLD_GROWTH = PlacementUtils.createKey("red_mushroom_old_growth");
    public static final ResourceKey<PlacedFeature> BROWN_MUSHROOM_SWAMP = PlacementUtils.createKey("brown_mushroom_swamp");
    public static final ResourceKey<PlacedFeature> RED_MUSHROOM_SWAMP = PlacementUtils.createKey("red_mushroom_swamp");
    public static final ResourceKey<PlacedFeature> FLOWER_WARM = PlacementUtils.createKey("flower_warm");
    public static final ResourceKey<PlacedFeature> FLOWER_DEFAULT = PlacementUtils.createKey("flower_default");
    public static final ResourceKey<PlacedFeature> FLOWER_FLOWER_FOREST = PlacementUtils.createKey("flower_flower_forest");
    public static final ResourceKey<PlacedFeature> FLOWER_SWAMP = PlacementUtils.createKey("flower_swamp");
    public static final ResourceKey<PlacedFeature> FLOWER_PLAINS = PlacementUtils.createKey("flower_plains");
    public static final ResourceKey<PlacedFeature> FLOWER_MEADOW = PlacementUtils.createKey("flower_meadow");
    public static final ResourceKey<PlacedFeature> FLOWER_CHERRY = PlacementUtils.createKey("flower_cherry");
    public static final ResourceKey<PlacedFeature> TREES_PLAINS = PlacementUtils.createKey("trees_plains");
    public static final ResourceKey<PlacedFeature> DARK_FOREST_VEGETATION = PlacementUtils.createKey("dark_forest_vegetation");
    public static final ResourceKey<PlacedFeature> FLOWER_FOREST_FLOWERS = PlacementUtils.createKey("flower_forest_flowers");
    public static final ResourceKey<PlacedFeature> FOREST_FLOWERS = PlacementUtils.createKey("forest_flowers");
    public static final ResourceKey<PlacedFeature> TREES_FLOWER_FOREST = PlacementUtils.createKey("trees_flower_forest");
    public static final ResourceKey<PlacedFeature> TREES_MEADOW = PlacementUtils.createKey("trees_meadow");
    public static final ResourceKey<PlacedFeature> TREES_CHERRY = PlacementUtils.createKey("trees_cherry");
    public static final ResourceKey<PlacedFeature> TREES_TAIGA = PlacementUtils.createKey("trees_taiga");
    public static final ResourceKey<PlacedFeature> TREES_GROVE = PlacementUtils.createKey("trees_grove");
    public static final ResourceKey<PlacedFeature> TREES_BADLANDS = PlacementUtils.createKey("trees_badlands");
    public static final ResourceKey<PlacedFeature> TREES_SNOWY = PlacementUtils.createKey("trees_snowy");
    public static final ResourceKey<PlacedFeature> TREES_SWAMP = PlacementUtils.createKey("trees_swamp");
    public static final ResourceKey<PlacedFeature> TREES_WINDSWEPT_SAVANNA = PlacementUtils.createKey("trees_windswept_savanna");
    public static final ResourceKey<PlacedFeature> TREES_SAVANNA = PlacementUtils.createKey("trees_savanna");
    public static final ResourceKey<PlacedFeature> BIRCH_TALL = PlacementUtils.createKey("birch_tall");
    public static final ResourceKey<PlacedFeature> TREES_BIRCH = PlacementUtils.createKey("trees_birch");
    public static final ResourceKey<PlacedFeature> TREES_WINDSWEPT_FOREST = PlacementUtils.createKey("trees_windswept_forest");
    public static final ResourceKey<PlacedFeature> TREES_WINDSWEPT_HILLS = PlacementUtils.createKey("trees_windswept_hills");
    public static final ResourceKey<PlacedFeature> TREES_WATER = PlacementUtils.createKey("trees_water");
    public static final ResourceKey<PlacedFeature> TREES_BIRCH_AND_OAK = PlacementUtils.createKey("trees_birch_and_oak");
    public static final ResourceKey<PlacedFeature> TREES_SPARSE_JUNGLE = PlacementUtils.createKey("trees_sparse_jungle");
    public static final ResourceKey<PlacedFeature> TREES_OLD_GROWTH_SPRUCE_TAIGA = PlacementUtils.createKey("trees_old_growth_spruce_taiga");
    public static final ResourceKey<PlacedFeature> TREES_OLD_GROWTH_PINE_TAIGA = PlacementUtils.createKey("trees_old_growth_pine_taiga");
    public static final ResourceKey<PlacedFeature> TREES_JUNGLE = PlacementUtils.createKey("trees_jungle");
    public static final ResourceKey<PlacedFeature> BAMBOO_VEGETATION = PlacementUtils.createKey("bamboo_vegetation");
    public static final ResourceKey<PlacedFeature> MUSHROOM_ISLAND_VEGETATION = PlacementUtils.createKey("mushroom_island_vegetation");
    public static final ResourceKey<PlacedFeature> TREES_MANGROVE = PlacementUtils.createKey("trees_mangrove");
    private static final PlacementModifier TREE_THRESHOLD = SurfaceWaterDepthFilter.forMaxDepth(0);

    public static List<PlacementModifier> worldSurfaceSquaredWithCount(int param0) {
        return List.of(CountPlacement.of(param0), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome());
    }

    private static List<PlacementModifier> getMushroomPlacement(int param0, @Nullable PlacementModifier param1) {
        Builder<PlacementModifier> var0 = ImmutableList.builder();
        if (param1 != null) {
            var0.add(param1);
        }

        if (param0 != 0) {
            var0.add(RarityFilter.onAverageOnceEvery(param0));
        }

        var0.add(InSquarePlacement.spread());
        var0.add(PlacementUtils.HEIGHTMAP);
        var0.add(BiomeFilter.biome());
        return var0.build();
    }

    private static Builder<PlacementModifier> treePlacementBase(PlacementModifier param0) {
        return ImmutableList.<PlacementModifier>builder()
            .add(param0)
            .add(InSquarePlacement.spread())
            .add(TREE_THRESHOLD)
            .add(PlacementUtils.HEIGHTMAP_OCEAN_FLOOR)
            .add(BiomeFilter.biome());
    }

    public static List<PlacementModifier> treePlacement(PlacementModifier param0) {
        return treePlacementBase(param0).build();
    }

    public static List<PlacementModifier> treePlacement(PlacementModifier param0, Block param1) {
        return treePlacementBase(param0).add(BlockPredicateFilter.forPredicate(BlockPredicate.wouldSurvive(param1.defaultBlockState(), BlockPos.ZERO))).build();
    }

    public static void bootstrap(BootstapContext<PlacedFeature> param0) {
        HolderGetter<ConfiguredFeature<?, ?>> var0 = param0.lookup(Registries.CONFIGURED_FEATURE);
        Holder<ConfiguredFeature<?, ?>> var1 = var0.getOrThrow(VegetationFeatures.BAMBOO_NO_PODZOL);
        Holder<ConfiguredFeature<?, ?>> var2 = var0.getOrThrow(VegetationFeatures.BAMBOO_SOME_PODZOL);
        Holder<ConfiguredFeature<?, ?>> var3 = var0.getOrThrow(VegetationFeatures.VINES);
        Holder<ConfiguredFeature<?, ?>> var4 = var0.getOrThrow(VegetationFeatures.PATCH_SUNFLOWER);
        Holder<ConfiguredFeature<?, ?>> var5 = var0.getOrThrow(VegetationFeatures.PATCH_PUMPKIN);
        Holder<ConfiguredFeature<?, ?>> var6 = var0.getOrThrow(VegetationFeatures.PATCH_GRASS);
        Holder<ConfiguredFeature<?, ?>> var7 = var0.getOrThrow(VegetationFeatures.PATCH_TAIGA_GRASS);
        Holder<ConfiguredFeature<?, ?>> var8 = var0.getOrThrow(VegetationFeatures.PATCH_GRASS_JUNGLE);
        Holder<ConfiguredFeature<?, ?>> var9 = var0.getOrThrow(VegetationFeatures.SINGLE_PIECE_OF_GRASS);
        Holder<ConfiguredFeature<?, ?>> var10 = var0.getOrThrow(VegetationFeatures.PATCH_DEAD_BUSH);
        Holder<ConfiguredFeature<?, ?>> var11 = var0.getOrThrow(VegetationFeatures.PATCH_MELON);
        Holder<ConfiguredFeature<?, ?>> var12 = var0.getOrThrow(VegetationFeatures.PATCH_BERRY_BUSH);
        Holder<ConfiguredFeature<?, ?>> var13 = var0.getOrThrow(VegetationFeatures.PATCH_WATERLILY);
        Holder<ConfiguredFeature<?, ?>> var14 = var0.getOrThrow(VegetationFeatures.PATCH_TALL_GRASS);
        Holder<ConfiguredFeature<?, ?>> var15 = var0.getOrThrow(VegetationFeatures.PATCH_LARGE_FERN);
        Holder<ConfiguredFeature<?, ?>> var16 = var0.getOrThrow(VegetationFeatures.PATCH_CACTUS);
        Holder<ConfiguredFeature<?, ?>> var17 = var0.getOrThrow(VegetationFeatures.PATCH_SUGAR_CANE);
        Holder<ConfiguredFeature<?, ?>> var18 = var0.getOrThrow(VegetationFeatures.PATCH_BROWN_MUSHROOM);
        Holder<ConfiguredFeature<?, ?>> var19 = var0.getOrThrow(VegetationFeatures.PATCH_RED_MUSHROOM);
        Holder<ConfiguredFeature<?, ?>> var20 = var0.getOrThrow(VegetationFeatures.FLOWER_DEFAULT);
        Holder<ConfiguredFeature<?, ?>> var21 = var0.getOrThrow(VegetationFeatures.FLOWER_FLOWER_FOREST);
        Holder<ConfiguredFeature<?, ?>> var22 = var0.getOrThrow(VegetationFeatures.FLOWER_SWAMP);
        Holder<ConfiguredFeature<?, ?>> var23 = var0.getOrThrow(VegetationFeatures.FLOWER_PLAIN);
        Holder<ConfiguredFeature<?, ?>> var24 = var0.getOrThrow(VegetationFeatures.FLOWER_MEADOW);
        Holder<ConfiguredFeature<?, ?>> var25 = var0.getOrThrow(VegetationFeatures.FLOWER_CHERRY);
        Holder<ConfiguredFeature<?, ?>> var26 = var0.getOrThrow(VegetationFeatures.TREES_PLAINS);
        Holder<ConfiguredFeature<?, ?>> var27 = var0.getOrThrow(VegetationFeatures.DARK_FOREST_VEGETATION);
        Holder<ConfiguredFeature<?, ?>> var28 = var0.getOrThrow(VegetationFeatures.FOREST_FLOWERS);
        Holder<ConfiguredFeature<?, ?>> var29 = var0.getOrThrow(VegetationFeatures.TREES_FLOWER_FOREST);
        Holder<ConfiguredFeature<?, ?>> var30 = var0.getOrThrow(VegetationFeatures.MEADOW_TREES);
        Holder<ConfiguredFeature<?, ?>> var31 = var0.getOrThrow(VegetationFeatures.TREES_TAIGA);
        Holder<ConfiguredFeature<?, ?>> var32 = var0.getOrThrow(VegetationFeatures.TREES_GROVE);
        Holder<ConfiguredFeature<?, ?>> var33 = var0.getOrThrow(TreeFeatures.OAK);
        Holder<ConfiguredFeature<?, ?>> var34 = var0.getOrThrow(TreeFeatures.SPRUCE);
        Holder<ConfiguredFeature<?, ?>> var35 = var0.getOrThrow(TreeFeatures.CHERRY_BEES_005);
        Holder<ConfiguredFeature<?, ?>> var36 = var0.getOrThrow(TreeFeatures.SWAMP_OAK);
        Holder<ConfiguredFeature<?, ?>> var37 = var0.getOrThrow(VegetationFeatures.TREES_SAVANNA);
        Holder<ConfiguredFeature<?, ?>> var38 = var0.getOrThrow(VegetationFeatures.BIRCH_TALL);
        Holder<ConfiguredFeature<?, ?>> var39 = var0.getOrThrow(TreeFeatures.BIRCH_BEES_0002);
        Holder<ConfiguredFeature<?, ?>> var40 = var0.getOrThrow(VegetationFeatures.TREES_WINDSWEPT_HILLS);
        Holder<ConfiguredFeature<?, ?>> var41 = var0.getOrThrow(VegetationFeatures.TREES_WATER);
        Holder<ConfiguredFeature<?, ?>> var42 = var0.getOrThrow(VegetationFeatures.TREES_BIRCH_AND_OAK);
        Holder<ConfiguredFeature<?, ?>> var43 = var0.getOrThrow(VegetationFeatures.TREES_SPARSE_JUNGLE);
        Holder<ConfiguredFeature<?, ?>> var44 = var0.getOrThrow(VegetationFeatures.TREES_OLD_GROWTH_SPRUCE_TAIGA);
        Holder<ConfiguredFeature<?, ?>> var45 = var0.getOrThrow(VegetationFeatures.TREES_OLD_GROWTH_PINE_TAIGA);
        Holder<ConfiguredFeature<?, ?>> var46 = var0.getOrThrow(VegetationFeatures.TREES_JUNGLE);
        Holder<ConfiguredFeature<?, ?>> var47 = var0.getOrThrow(VegetationFeatures.BAMBOO_VEGETATION);
        Holder<ConfiguredFeature<?, ?>> var48 = var0.getOrThrow(VegetationFeatures.MUSHROOM_ISLAND_VEGETATION);
        Holder<ConfiguredFeature<?, ?>> var49 = var0.getOrThrow(VegetationFeatures.MANGROVE_VEGETATION);
        PlacementUtils.register(
            param0, BAMBOO_LIGHT, var1, RarityFilter.onAverageOnceEvery(4), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
        );
        PlacementUtils.register(
            param0,
            BAMBOO,
            var2,
            NoiseBasedCountPlacement.of(160, 80.0, 0.3),
            InSquarePlacement.spread(),
            PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
            BiomeFilter.biome()
        );
        PlacementUtils.register(
            param0,
            VINES,
            var3,
            CountPlacement.of(127),
            InSquarePlacement.spread(),
            HeightRangePlacement.uniform(VerticalAnchor.absolute(64), VerticalAnchor.absolute(100)),
            BiomeFilter.biome()
        );
        PlacementUtils.register(
            param0, PATCH_SUNFLOWER, var4, RarityFilter.onAverageOnceEvery(3), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
        );
        PlacementUtils.register(
            param0, PATCH_PUMPKIN, var5, RarityFilter.onAverageOnceEvery(300), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
        );
        PlacementUtils.register(
            param0,
            PATCH_GRASS_PLAIN,
            var6,
            NoiseThresholdCountPlacement.of(-0.8, 5, 10),
            InSquarePlacement.spread(),
            PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
            BiomeFilter.biome()
        );
        PlacementUtils.register(param0, PATCH_GRASS_FOREST, var6, worldSurfaceSquaredWithCount(2));
        PlacementUtils.register(param0, PATCH_GRASS_BADLANDS, var6, InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome());
        PlacementUtils.register(param0, PATCH_GRASS_SAVANNA, var6, worldSurfaceSquaredWithCount(20));
        PlacementUtils.register(param0, PATCH_GRASS_NORMAL, var6, worldSurfaceSquaredWithCount(5));
        PlacementUtils.register(param0, PATCH_GRASS_TAIGA_2, var7, InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome());
        PlacementUtils.register(param0, PATCH_GRASS_TAIGA, var7, worldSurfaceSquaredWithCount(7));
        PlacementUtils.register(param0, PATCH_GRASS_JUNGLE, var8, worldSurfaceSquaredWithCount(25));
        PlacementUtils.register(param0, GRASS_BONEMEAL, var9, PlacementUtils.isEmpty());
        PlacementUtils.register(param0, PATCH_DEAD_BUSH_2, var10, worldSurfaceSquaredWithCount(2));
        PlacementUtils.register(param0, PATCH_DEAD_BUSH, var10, InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome());
        PlacementUtils.register(param0, PATCH_DEAD_BUSH_BADLANDS, var10, worldSurfaceSquaredWithCount(20));
        PlacementUtils.register(
            param0, PATCH_MELON, var11, RarityFilter.onAverageOnceEvery(6), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
        );
        PlacementUtils.register(
            param0, PATCH_MELON_SPARSE, var11, RarityFilter.onAverageOnceEvery(64), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
        );
        PlacementUtils.register(
            param0,
            PATCH_BERRY_COMMON,
            var12,
            RarityFilter.onAverageOnceEvery(32),
            InSquarePlacement.spread(),
            PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
            BiomeFilter.biome()
        );
        PlacementUtils.register(
            param0,
            PATCH_BERRY_RARE,
            var12,
            RarityFilter.onAverageOnceEvery(384),
            InSquarePlacement.spread(),
            PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
            BiomeFilter.biome()
        );
        PlacementUtils.register(param0, PATCH_WATERLILY, var13, worldSurfaceSquaredWithCount(4));
        PlacementUtils.register(
            param0,
            PATCH_TALL_GRASS_2,
            var14,
            NoiseThresholdCountPlacement.of(-0.8, 0, 7),
            RarityFilter.onAverageOnceEvery(32),
            InSquarePlacement.spread(),
            PlacementUtils.HEIGHTMAP,
            BiomeFilter.biome()
        );
        PlacementUtils.register(
            param0, PATCH_TALL_GRASS, var14, RarityFilter.onAverageOnceEvery(5), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
        );
        PlacementUtils.register(
            param0, PATCH_LARGE_FERN, var15, RarityFilter.onAverageOnceEvery(5), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
        );
        PlacementUtils.register(
            param0, PATCH_CACTUS_DESERT, var16, RarityFilter.onAverageOnceEvery(6), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
        );
        PlacementUtils.register(
            param0,
            PATCH_CACTUS_DECORATED,
            var16,
            RarityFilter.onAverageOnceEvery(13),
            InSquarePlacement.spread(),
            PlacementUtils.HEIGHTMAP,
            BiomeFilter.biome()
        );
        PlacementUtils.register(
            param0,
            PATCH_SUGAR_CANE_SWAMP,
            var17,
            RarityFilter.onAverageOnceEvery(3),
            InSquarePlacement.spread(),
            PlacementUtils.HEIGHTMAP,
            BiomeFilter.biome()
        );
        PlacementUtils.register(param0, PATCH_SUGAR_CANE_DESERT, var17, InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(
            param0,
            PATCH_SUGAR_CANE_BADLANDS,
            var17,
            RarityFilter.onAverageOnceEvery(5),
            InSquarePlacement.spread(),
            PlacementUtils.HEIGHTMAP,
            BiomeFilter.biome()
        );
        PlacementUtils.register(
            param0, PATCH_SUGAR_CANE, var17, RarityFilter.onAverageOnceEvery(6), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
        );
        PlacementUtils.register(
            param0,
            BROWN_MUSHROOM_NETHER,
            var18,
            RarityFilter.onAverageOnceEvery(2),
            InSquarePlacement.spread(),
            PlacementUtils.FULL_RANGE,
            BiomeFilter.biome()
        );
        PlacementUtils.register(
            param0, RED_MUSHROOM_NETHER, var19, RarityFilter.onAverageOnceEvery(2), InSquarePlacement.spread(), PlacementUtils.FULL_RANGE, BiomeFilter.biome()
        );
        PlacementUtils.register(param0, BROWN_MUSHROOM_NORMAL, var18, getMushroomPlacement(256, null));
        PlacementUtils.register(param0, RED_MUSHROOM_NORMAL, var19, getMushroomPlacement(512, null));
        PlacementUtils.register(param0, BROWN_MUSHROOM_TAIGA, var18, getMushroomPlacement(4, null));
        PlacementUtils.register(param0, RED_MUSHROOM_TAIGA, var19, getMushroomPlacement(256, null));
        PlacementUtils.register(param0, BROWN_MUSHROOM_OLD_GROWTH, var18, getMushroomPlacement(4, CountPlacement.of(3)));
        PlacementUtils.register(param0, RED_MUSHROOM_OLD_GROWTH, var19, getMushroomPlacement(171, null));
        PlacementUtils.register(param0, BROWN_MUSHROOM_SWAMP, var18, getMushroomPlacement(0, CountPlacement.of(2)));
        PlacementUtils.register(param0, RED_MUSHROOM_SWAMP, var19, getMushroomPlacement(64, null));
        PlacementUtils.register(
            param0, FLOWER_WARM, var20, RarityFilter.onAverageOnceEvery(16), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
        );
        PlacementUtils.register(
            param0, FLOWER_DEFAULT, var20, RarityFilter.onAverageOnceEvery(32), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
        );
        PlacementUtils.register(
            param0,
            FLOWER_FLOWER_FOREST,
            var21,
            CountPlacement.of(3),
            RarityFilter.onAverageOnceEvery(2),
            InSquarePlacement.spread(),
            PlacementUtils.HEIGHTMAP,
            BiomeFilter.biome()
        );
        PlacementUtils.register(
            param0, FLOWER_SWAMP, var22, RarityFilter.onAverageOnceEvery(32), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
        );
        PlacementUtils.register(
            param0,
            FLOWER_PLAINS,
            var23,
            NoiseThresholdCountPlacement.of(-0.8, 15, 4),
            RarityFilter.onAverageOnceEvery(32),
            InSquarePlacement.spread(),
            PlacementUtils.HEIGHTMAP,
            BiomeFilter.biome()
        );
        PlacementUtils.register(
            param0,
            FLOWER_CHERRY,
            var25,
            NoiseThresholdCountPlacement.of(-0.8, 5, 10),
            InSquarePlacement.spread(),
            PlacementUtils.HEIGHTMAP,
            BiomeFilter.biome()
        );
        PlacementUtils.register(param0, FLOWER_MEADOW, var24, InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementModifier var50 = SurfaceWaterDepthFilter.forMaxDepth(0);
        PlacementUtils.register(
            param0,
            TREES_PLAINS,
            var26,
            PlacementUtils.countExtra(0, 0.05F, 1),
            InSquarePlacement.spread(),
            var50,
            PlacementUtils.HEIGHTMAP_OCEAN_FLOOR,
            BlockPredicateFilter.forPredicate(BlockPredicate.wouldSurvive(Blocks.OAK_SAPLING.defaultBlockState(), BlockPos.ZERO)),
            BiomeFilter.biome()
        );
        PlacementUtils.register(
            param0,
            DARK_FOREST_VEGETATION,
            var27,
            CountPlacement.of(16),
            InSquarePlacement.spread(),
            var50,
            PlacementUtils.HEIGHTMAP_OCEAN_FLOOR,
            BiomeFilter.biome()
        );
        PlacementUtils.register(
            param0,
            FLOWER_FOREST_FLOWERS,
            var28,
            RarityFilter.onAverageOnceEvery(7),
            InSquarePlacement.spread(),
            PlacementUtils.HEIGHTMAP,
            CountPlacement.of(ClampedInt.of(UniformInt.of(-1, 3), 0, 3)),
            BiomeFilter.biome()
        );
        PlacementUtils.register(
            param0,
            FOREST_FLOWERS,
            var28,
            RarityFilter.onAverageOnceEvery(7),
            InSquarePlacement.spread(),
            PlacementUtils.HEIGHTMAP,
            CountPlacement.of(ClampedInt.of(UniformInt.of(-3, 1), 0, 1)),
            BiomeFilter.biome()
        );
        PlacementUtils.register(param0, TREES_FLOWER_FOREST, var29, treePlacement(PlacementUtils.countExtra(6, 0.1F, 1)));
        PlacementUtils.register(param0, TREES_MEADOW, var30, treePlacement(RarityFilter.onAverageOnceEvery(100)));
        PlacementUtils.register(param0, TREES_CHERRY, var35, treePlacement(PlacementUtils.countExtra(10, 0.1F, 1), Blocks.CHERRY_SAPLING));
        PlacementUtils.register(param0, TREES_TAIGA, var31, treePlacement(PlacementUtils.countExtra(10, 0.1F, 1)));
        PlacementUtils.register(param0, TREES_GROVE, var32, treePlacement(PlacementUtils.countExtra(10, 0.1F, 1)));
        PlacementUtils.register(param0, TREES_BADLANDS, var33, treePlacement(PlacementUtils.countExtra(5, 0.1F, 1), Blocks.OAK_SAPLING));
        PlacementUtils.register(param0, TREES_SNOWY, var34, treePlacement(PlacementUtils.countExtra(0, 0.1F, 1), Blocks.SPRUCE_SAPLING));
        PlacementUtils.register(
            param0,
            TREES_SWAMP,
            var36,
            PlacementUtils.countExtra(2, 0.1F, 1),
            InSquarePlacement.spread(),
            SurfaceWaterDepthFilter.forMaxDepth(2),
            PlacementUtils.HEIGHTMAP_OCEAN_FLOOR,
            BiomeFilter.biome(),
            BlockPredicateFilter.forPredicate(BlockPredicate.wouldSurvive(Blocks.OAK_SAPLING.defaultBlockState(), BlockPos.ZERO))
        );
        PlacementUtils.register(param0, TREES_WINDSWEPT_SAVANNA, var37, treePlacement(PlacementUtils.countExtra(2, 0.1F, 1)));
        PlacementUtils.register(param0, TREES_SAVANNA, var37, treePlacement(PlacementUtils.countExtra(1, 0.1F, 1)));
        PlacementUtils.register(param0, BIRCH_TALL, var38, treePlacement(PlacementUtils.countExtra(10, 0.1F, 1)));
        PlacementUtils.register(param0, TREES_BIRCH, var39, treePlacement(PlacementUtils.countExtra(10, 0.1F, 1), Blocks.BIRCH_SAPLING));
        PlacementUtils.register(param0, TREES_WINDSWEPT_FOREST, var40, treePlacement(PlacementUtils.countExtra(3, 0.1F, 1)));
        PlacementUtils.register(param0, TREES_WINDSWEPT_HILLS, var40, treePlacement(PlacementUtils.countExtra(0, 0.1F, 1)));
        PlacementUtils.register(param0, TREES_WATER, var41, treePlacement(PlacementUtils.countExtra(0, 0.1F, 1)));
        PlacementUtils.register(param0, TREES_BIRCH_AND_OAK, var42, treePlacement(PlacementUtils.countExtra(10, 0.1F, 1)));
        PlacementUtils.register(param0, TREES_SPARSE_JUNGLE, var43, treePlacement(PlacementUtils.countExtra(2, 0.1F, 1)));
        PlacementUtils.register(param0, TREES_OLD_GROWTH_SPRUCE_TAIGA, var44, treePlacement(PlacementUtils.countExtra(10, 0.1F, 1)));
        PlacementUtils.register(param0, TREES_OLD_GROWTH_PINE_TAIGA, var45, treePlacement(PlacementUtils.countExtra(10, 0.1F, 1)));
        PlacementUtils.register(param0, TREES_JUNGLE, var46, treePlacement(PlacementUtils.countExtra(50, 0.1F, 1)));
        PlacementUtils.register(param0, BAMBOO_VEGETATION, var47, treePlacement(PlacementUtils.countExtra(30, 0.1F, 1)));
        PlacementUtils.register(param0, MUSHROOM_ISLAND_VEGETATION, var48, InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        PlacementUtils.register(
            param0,
            TREES_MANGROVE,
            var49,
            CountPlacement.of(25),
            InSquarePlacement.spread(),
            SurfaceWaterDepthFilter.forMaxDepth(5),
            PlacementUtils.HEIGHTMAP_OCEAN_FLOOR,
            BiomeFilter.biome(),
            BlockPredicateFilter.forPredicate(BlockPredicate.wouldSurvive(Blocks.MANGROVE_PROPAGULE.defaultBlockState(), BlockPos.ZERO))
        );
    }
}

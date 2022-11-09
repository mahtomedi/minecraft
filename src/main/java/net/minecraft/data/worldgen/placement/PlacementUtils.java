package net.minecraft.data.worldgen.placement;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.WeightedListInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.HeightmapPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

public class PlacementUtils {
    public static final PlacementModifier HEIGHTMAP = HeightmapPlacement.onHeightmap(Heightmap.Types.MOTION_BLOCKING);
    public static final PlacementModifier HEIGHTMAP_TOP_SOLID = HeightmapPlacement.onHeightmap(Heightmap.Types.OCEAN_FLOOR_WG);
    public static final PlacementModifier HEIGHTMAP_WORLD_SURFACE = HeightmapPlacement.onHeightmap(Heightmap.Types.WORLD_SURFACE_WG);
    public static final PlacementModifier HEIGHTMAP_OCEAN_FLOOR = HeightmapPlacement.onHeightmap(Heightmap.Types.OCEAN_FLOOR);
    public static final PlacementModifier FULL_RANGE = HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.top());
    public static final PlacementModifier RANGE_10_10 = HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(10), VerticalAnchor.belowTop(10));
    public static final PlacementModifier RANGE_8_8 = HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(8), VerticalAnchor.belowTop(8));
    public static final PlacementModifier RANGE_4_4 = HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(4), VerticalAnchor.belowTop(4));
    public static final PlacementModifier RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT = HeightRangePlacement.uniform(
        VerticalAnchor.bottom(), VerticalAnchor.absolute(256)
    );

    public static void bootstrap(BootstapContext<PlacedFeature> param0) {
        AquaticPlacements.bootstrap(param0);
        CavePlacements.bootstrap(param0);
        EndPlacements.bootstrap(param0);
        MiscOverworldPlacements.bootstrap(param0);
        NetherPlacements.bootstrap(param0);
        OrePlacements.bootstrap(param0);
        TreePlacements.bootstrap(param0);
        VegetationPlacements.bootstrap(param0);
        VillagePlacements.bootstrap(param0);
    }

    public static ResourceKey<PlacedFeature> createKey(String param0) {
        return ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(param0));
    }

    public static void register(
        BootstapContext<PlacedFeature> param0, ResourceKey<PlacedFeature> param1, Holder<ConfiguredFeature<?, ?>> param2, List<PlacementModifier> param3
    ) {
        param0.register(param1, new PlacedFeature(param2, List.copyOf(param3)));
    }

    public static void register(
        BootstapContext<PlacedFeature> param0, ResourceKey<PlacedFeature> param1, Holder<ConfiguredFeature<?, ?>> param2, PlacementModifier... param3
    ) {
        register(param0, param1, param2, List.of(param3));
    }

    public static PlacementModifier countExtra(int param0, float param1, int param2) {
        float var0 = 1.0F / param1;
        if (Math.abs(var0 - (float)((int)var0)) > 1.0E-5F) {
            throw new IllegalStateException("Chance data cannot be represented as list weight");
        } else {
            SimpleWeightedRandomList<IntProvider> var1 = SimpleWeightedRandomList.<IntProvider>builder()
                .add(ConstantInt.of(param0), (int)var0 - 1)
                .add(ConstantInt.of(param0 + param2), 1)
                .build();
            return CountPlacement.of(new WeightedListInt(var1));
        }
    }

    public static PlacementFilter isEmpty() {
        return BlockPredicateFilter.forPredicate(BlockPredicate.ONLY_IN_AIR_PREDICATE);
    }

    public static BlockPredicateFilter filteredByBlockSurvival(Block param0) {
        return BlockPredicateFilter.forPredicate(BlockPredicate.wouldSurvive(param0.defaultBlockState(), BlockPos.ZERO));
    }

    public static Holder<PlacedFeature> inlinePlaced(Holder<ConfiguredFeature<?, ?>> param0, PlacementModifier... param1) {
        return Holder.direct(new PlacedFeature(param0, List.of(param1)));
    }

    public static <FC extends FeatureConfiguration, F extends Feature<FC>> Holder<PlacedFeature> inlinePlaced(F param0, FC param1, PlacementModifier... param2) {
        return inlinePlaced(Holder.direct(new ConfiguredFeature(param0, param1)), param2);
    }

    public static <FC extends FeatureConfiguration, F extends Feature<FC>> Holder<PlacedFeature> onlyWhenEmpty(F param0, FC param1) {
        return filtered(param0, param1, BlockPredicate.ONLY_IN_AIR_PREDICATE);
    }

    public static <FC extends FeatureConfiguration, F extends Feature<FC>> Holder<PlacedFeature> filtered(F param0, FC param1, BlockPredicate param2) {
        return inlinePlaced(param0, param1, BlockPredicateFilter.forPredicate(param2));
    }
}

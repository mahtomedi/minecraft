package net.minecraft.data.worldgen.placement;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.features.NetherFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.BiasedToBottomInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountOnEveryLayerPlacement;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

public class NetherPlacements {
    public static final ResourceKey<PlacedFeature> DELTA = PlacementUtils.createKey("delta");
    public static final ResourceKey<PlacedFeature> SMALL_BASALT_COLUMNS = PlacementUtils.createKey("small_basalt_columns");
    public static final ResourceKey<PlacedFeature> LARGE_BASALT_COLUMNS = PlacementUtils.createKey("large_basalt_columns");
    public static final ResourceKey<PlacedFeature> BASALT_BLOBS = PlacementUtils.createKey("basalt_blobs");
    public static final ResourceKey<PlacedFeature> BLACKSTONE_BLOBS = PlacementUtils.createKey("blackstone_blobs");
    public static final ResourceKey<PlacedFeature> GLOWSTONE_EXTRA = PlacementUtils.createKey("glowstone_extra");
    public static final ResourceKey<PlacedFeature> GLOWSTONE = PlacementUtils.createKey("glowstone");
    public static final ResourceKey<PlacedFeature> CRIMSON_FOREST_VEGETATION = PlacementUtils.createKey("crimson_forest_vegetation");
    public static final ResourceKey<PlacedFeature> WARPED_FOREST_VEGETATION = PlacementUtils.createKey("warped_forest_vegetation");
    public static final ResourceKey<PlacedFeature> NETHER_SPROUTS = PlacementUtils.createKey("nether_sprouts");
    public static final ResourceKey<PlacedFeature> TWISTING_VINES = PlacementUtils.createKey("twisting_vines");
    public static final ResourceKey<PlacedFeature> WEEPING_VINES = PlacementUtils.createKey("weeping_vines");
    public static final ResourceKey<PlacedFeature> PATCH_CRIMSON_ROOTS = PlacementUtils.createKey("patch_crimson_roots");
    public static final ResourceKey<PlacedFeature> BASALT_PILLAR = PlacementUtils.createKey("basalt_pillar");
    public static final ResourceKey<PlacedFeature> SPRING_DELTA = PlacementUtils.createKey("spring_delta");
    public static final ResourceKey<PlacedFeature> SPRING_CLOSED = PlacementUtils.createKey("spring_closed");
    public static final ResourceKey<PlacedFeature> SPRING_CLOSED_DOUBLE = PlacementUtils.createKey("spring_closed_double");
    public static final ResourceKey<PlacedFeature> SPRING_OPEN = PlacementUtils.createKey("spring_open");
    public static final ResourceKey<PlacedFeature> PATCH_SOUL_FIRE = PlacementUtils.createKey("patch_soul_fire");
    public static final ResourceKey<PlacedFeature> PATCH_FIRE = PlacementUtils.createKey("patch_fire");

    public static void bootstrap(BootstapContext<PlacedFeature> param0) {
        HolderGetter<ConfiguredFeature<?, ?>> var0 = param0.lookup(Registries.CONFIGURED_FEATURE);
        Holder<ConfiguredFeature<?, ?>> var1 = var0.getOrThrow(NetherFeatures.DELTA);
        Holder<ConfiguredFeature<?, ?>> var2 = var0.getOrThrow(NetherFeatures.SMALL_BASALT_COLUMNS);
        Holder<ConfiguredFeature<?, ?>> var3 = var0.getOrThrow(NetherFeatures.LARGE_BASALT_COLUMNS);
        Holder<ConfiguredFeature<?, ?>> var4 = var0.getOrThrow(NetherFeatures.BASALT_BLOBS);
        Holder<ConfiguredFeature<?, ?>> var5 = var0.getOrThrow(NetherFeatures.BLACKSTONE_BLOBS);
        Holder<ConfiguredFeature<?, ?>> var6 = var0.getOrThrow(NetherFeatures.GLOWSTONE_EXTRA);
        Holder<ConfiguredFeature<?, ?>> var7 = var0.getOrThrow(NetherFeatures.CRIMSON_FOREST_VEGETATION);
        Holder<ConfiguredFeature<?, ?>> var8 = var0.getOrThrow(NetherFeatures.WARPED_FOREST_VEGETION);
        Holder<ConfiguredFeature<?, ?>> var9 = var0.getOrThrow(NetherFeatures.NETHER_SPROUTS);
        Holder<ConfiguredFeature<?, ?>> var10 = var0.getOrThrow(NetherFeatures.TWISTING_VINES);
        Holder<ConfiguredFeature<?, ?>> var11 = var0.getOrThrow(NetherFeatures.WEEPING_VINES);
        Holder<ConfiguredFeature<?, ?>> var12 = var0.getOrThrow(NetherFeatures.PATCH_CRIMSON_ROOTS);
        Holder<ConfiguredFeature<?, ?>> var13 = var0.getOrThrow(NetherFeatures.BASALT_PILLAR);
        Holder<ConfiguredFeature<?, ?>> var14 = var0.getOrThrow(NetherFeatures.SPRING_LAVA_NETHER);
        Holder<ConfiguredFeature<?, ?>> var15 = var0.getOrThrow(NetherFeatures.SPRING_NETHER_CLOSED);
        Holder<ConfiguredFeature<?, ?>> var16 = var0.getOrThrow(NetherFeatures.SPRING_NETHER_OPEN);
        Holder<ConfiguredFeature<?, ?>> var17 = var0.getOrThrow(NetherFeatures.PATCH_SOUL_FIRE);
        Holder<ConfiguredFeature<?, ?>> var18 = var0.getOrThrow(NetherFeatures.PATCH_FIRE);
        PlacementUtils.register(param0, DELTA, var1, CountOnEveryLayerPlacement.of(40), BiomeFilter.biome());
        PlacementUtils.register(param0, SMALL_BASALT_COLUMNS, var2, CountOnEveryLayerPlacement.of(4), BiomeFilter.biome());
        PlacementUtils.register(param0, LARGE_BASALT_COLUMNS, var3, CountOnEveryLayerPlacement.of(2), BiomeFilter.biome());
        PlacementUtils.register(param0, BASALT_BLOBS, var4, CountPlacement.of(75), InSquarePlacement.spread(), PlacementUtils.FULL_RANGE, BiomeFilter.biome());
        PlacementUtils.register(
            param0, BLACKSTONE_BLOBS, var5, CountPlacement.of(25), InSquarePlacement.spread(), PlacementUtils.FULL_RANGE, BiomeFilter.biome()
        );
        PlacementUtils.register(
            param0,
            GLOWSTONE_EXTRA,
            var6,
            CountPlacement.of(BiasedToBottomInt.of(0, 9)),
            InSquarePlacement.spread(),
            PlacementUtils.RANGE_4_4,
            BiomeFilter.biome()
        );
        PlacementUtils.register(param0, GLOWSTONE, var6, CountPlacement.of(10), InSquarePlacement.spread(), PlacementUtils.FULL_RANGE, BiomeFilter.biome());
        PlacementUtils.register(param0, CRIMSON_FOREST_VEGETATION, var7, CountOnEveryLayerPlacement.of(6), BiomeFilter.biome());
        PlacementUtils.register(param0, WARPED_FOREST_VEGETATION, var8, CountOnEveryLayerPlacement.of(5), BiomeFilter.biome());
        PlacementUtils.register(param0, NETHER_SPROUTS, var9, CountOnEveryLayerPlacement.of(4), BiomeFilter.biome());
        PlacementUtils.register(
            param0, TWISTING_VINES, var10, CountPlacement.of(10), InSquarePlacement.spread(), PlacementUtils.FULL_RANGE, BiomeFilter.biome()
        );
        PlacementUtils.register(param0, WEEPING_VINES, var11, CountPlacement.of(10), InSquarePlacement.spread(), PlacementUtils.FULL_RANGE, BiomeFilter.biome());
        PlacementUtils.register(param0, PATCH_CRIMSON_ROOTS, var12, PlacementUtils.FULL_RANGE, BiomeFilter.biome());
        PlacementUtils.register(param0, BASALT_PILLAR, var13, CountPlacement.of(10), InSquarePlacement.spread(), PlacementUtils.FULL_RANGE, BiomeFilter.biome());
        PlacementUtils.register(param0, SPRING_DELTA, var14, CountPlacement.of(16), InSquarePlacement.spread(), PlacementUtils.RANGE_4_4, BiomeFilter.biome());
        PlacementUtils.register(
            param0, SPRING_CLOSED, var15, CountPlacement.of(16), InSquarePlacement.spread(), PlacementUtils.RANGE_10_10, BiomeFilter.biome()
        );
        PlacementUtils.register(
            param0, SPRING_CLOSED_DOUBLE, var15, CountPlacement.of(32), InSquarePlacement.spread(), PlacementUtils.RANGE_10_10, BiomeFilter.biome()
        );
        PlacementUtils.register(param0, SPRING_OPEN, var16, CountPlacement.of(8), InSquarePlacement.spread(), PlacementUtils.RANGE_4_4, BiomeFilter.biome());
        List<PlacementModifier> var19 = List.of(
            CountPlacement.of(UniformInt.of(0, 5)), InSquarePlacement.spread(), PlacementUtils.RANGE_4_4, BiomeFilter.biome()
        );
        PlacementUtils.register(param0, PATCH_SOUL_FIRE, var17, var19);
        PlacementUtils.register(param0, PATCH_FIRE, var18, var19);
    }
}

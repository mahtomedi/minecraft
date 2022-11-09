package net.minecraft.data.worldgen.placement;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;

public class OrePlacements {
    public static final ResourceKey<PlacedFeature> ORE_MAGMA = PlacementUtils.createKey("ore_magma");
    public static final ResourceKey<PlacedFeature> ORE_SOUL_SAND = PlacementUtils.createKey("ore_soul_sand");
    public static final ResourceKey<PlacedFeature> ORE_GOLD_DELTAS = PlacementUtils.createKey("ore_gold_deltas");
    public static final ResourceKey<PlacedFeature> ORE_QUARTZ_DELTAS = PlacementUtils.createKey("ore_quartz_deltas");
    public static final ResourceKey<PlacedFeature> ORE_GOLD_NETHER = PlacementUtils.createKey("ore_gold_nether");
    public static final ResourceKey<PlacedFeature> ORE_QUARTZ_NETHER = PlacementUtils.createKey("ore_quartz_nether");
    public static final ResourceKey<PlacedFeature> ORE_GRAVEL_NETHER = PlacementUtils.createKey("ore_gravel_nether");
    public static final ResourceKey<PlacedFeature> ORE_BLACKSTONE = PlacementUtils.createKey("ore_blackstone");
    public static final ResourceKey<PlacedFeature> ORE_DIRT = PlacementUtils.createKey("ore_dirt");
    public static final ResourceKey<PlacedFeature> ORE_GRAVEL = PlacementUtils.createKey("ore_gravel");
    public static final ResourceKey<PlacedFeature> ORE_GRANITE_UPPER = PlacementUtils.createKey("ore_granite_upper");
    public static final ResourceKey<PlacedFeature> ORE_GRANITE_LOWER = PlacementUtils.createKey("ore_granite_lower");
    public static final ResourceKey<PlacedFeature> ORE_DIORITE_UPPER = PlacementUtils.createKey("ore_diorite_upper");
    public static final ResourceKey<PlacedFeature> ORE_DIORITE_LOWER = PlacementUtils.createKey("ore_diorite_lower");
    public static final ResourceKey<PlacedFeature> ORE_ANDESITE_UPPER = PlacementUtils.createKey("ore_andesite_upper");
    public static final ResourceKey<PlacedFeature> ORE_ANDESITE_LOWER = PlacementUtils.createKey("ore_andesite_lower");
    public static final ResourceKey<PlacedFeature> ORE_TUFF = PlacementUtils.createKey("ore_tuff");
    public static final ResourceKey<PlacedFeature> ORE_COAL_UPPER = PlacementUtils.createKey("ore_coal_upper");
    public static final ResourceKey<PlacedFeature> ORE_COAL_LOWER = PlacementUtils.createKey("ore_coal_lower");
    public static final ResourceKey<PlacedFeature> ORE_IRON_UPPER = PlacementUtils.createKey("ore_iron_upper");
    public static final ResourceKey<PlacedFeature> ORE_IRON_MIDDLE = PlacementUtils.createKey("ore_iron_middle");
    public static final ResourceKey<PlacedFeature> ORE_IRON_SMALL = PlacementUtils.createKey("ore_iron_small");
    public static final ResourceKey<PlacedFeature> ORE_GOLD_EXTRA = PlacementUtils.createKey("ore_gold_extra");
    public static final ResourceKey<PlacedFeature> ORE_GOLD = PlacementUtils.createKey("ore_gold");
    public static final ResourceKey<PlacedFeature> ORE_GOLD_LOWER = PlacementUtils.createKey("ore_gold_lower");
    public static final ResourceKey<PlacedFeature> ORE_REDSTONE = PlacementUtils.createKey("ore_redstone");
    public static final ResourceKey<PlacedFeature> ORE_REDSTONE_LOWER = PlacementUtils.createKey("ore_redstone_lower");
    public static final ResourceKey<PlacedFeature> ORE_DIAMOND = PlacementUtils.createKey("ore_diamond");
    public static final ResourceKey<PlacedFeature> ORE_DIAMOND_LARGE = PlacementUtils.createKey("ore_diamond_large");
    public static final ResourceKey<PlacedFeature> ORE_DIAMOND_BURIED = PlacementUtils.createKey("ore_diamond_buried");
    public static final ResourceKey<PlacedFeature> ORE_LAPIS = PlacementUtils.createKey("ore_lapis");
    public static final ResourceKey<PlacedFeature> ORE_LAPIS_BURIED = PlacementUtils.createKey("ore_lapis_buried");
    public static final ResourceKey<PlacedFeature> ORE_INFESTED = PlacementUtils.createKey("ore_infested");
    public static final ResourceKey<PlacedFeature> ORE_EMERALD = PlacementUtils.createKey("ore_emerald");
    public static final ResourceKey<PlacedFeature> ORE_ANCIENT_DEBRIS_LARGE = PlacementUtils.createKey("ore_ancient_debris_large");
    public static final ResourceKey<PlacedFeature> ORE_ANCIENT_DEBRIS_SMALL = PlacementUtils.createKey("ore_debris_small");
    public static final ResourceKey<PlacedFeature> ORE_COPPER = PlacementUtils.createKey("ore_copper");
    public static final ResourceKey<PlacedFeature> ORE_COPPER_LARGE = PlacementUtils.createKey("ore_copper_large");
    public static final ResourceKey<PlacedFeature> ORE_CLAY = PlacementUtils.createKey("ore_clay");

    private static List<PlacementModifier> orePlacement(PlacementModifier param0, PlacementModifier param1) {
        return List.of(param0, InSquarePlacement.spread(), param1, BiomeFilter.biome());
    }

    private static List<PlacementModifier> commonOrePlacement(int param0, PlacementModifier param1) {
        return orePlacement(CountPlacement.of(param0), param1);
    }

    private static List<PlacementModifier> rareOrePlacement(int param0, PlacementModifier param1) {
        return orePlacement(RarityFilter.onAverageOnceEvery(param0), param1);
    }

    public static void bootstrap(BootstapContext<PlacedFeature> param0) {
        HolderGetter<ConfiguredFeature<?, ?>> var0 = param0.lookup(Registries.CONFIGURED_FEATURE);
        Holder<ConfiguredFeature<?, ?>> var1 = var0.getOrThrow(OreFeatures.ORE_MAGMA);
        Holder<ConfiguredFeature<?, ?>> var2 = var0.getOrThrow(OreFeatures.ORE_SOUL_SAND);
        Holder<ConfiguredFeature<?, ?>> var3 = var0.getOrThrow(OreFeatures.ORE_NETHER_GOLD);
        Holder<ConfiguredFeature<?, ?>> var4 = var0.getOrThrow(OreFeatures.ORE_QUARTZ);
        Holder<ConfiguredFeature<?, ?>> var5 = var0.getOrThrow(OreFeatures.ORE_GRAVEL_NETHER);
        Holder<ConfiguredFeature<?, ?>> var6 = var0.getOrThrow(OreFeatures.ORE_BLACKSTONE);
        Holder<ConfiguredFeature<?, ?>> var7 = var0.getOrThrow(OreFeatures.ORE_DIRT);
        Holder<ConfiguredFeature<?, ?>> var8 = var0.getOrThrow(OreFeatures.ORE_GRAVEL);
        Holder<ConfiguredFeature<?, ?>> var9 = var0.getOrThrow(OreFeatures.ORE_GRANITE);
        Holder<ConfiguredFeature<?, ?>> var10 = var0.getOrThrow(OreFeatures.ORE_DIORITE);
        Holder<ConfiguredFeature<?, ?>> var11 = var0.getOrThrow(OreFeatures.ORE_ANDESITE);
        Holder<ConfiguredFeature<?, ?>> var12 = var0.getOrThrow(OreFeatures.ORE_TUFF);
        Holder<ConfiguredFeature<?, ?>> var13 = var0.getOrThrow(OreFeatures.ORE_COAL);
        Holder<ConfiguredFeature<?, ?>> var14 = var0.getOrThrow(OreFeatures.ORE_COAL_BURIED);
        Holder<ConfiguredFeature<?, ?>> var15 = var0.getOrThrow(OreFeatures.ORE_IRON);
        Holder<ConfiguredFeature<?, ?>> var16 = var0.getOrThrow(OreFeatures.ORE_IRON_SMALL);
        Holder<ConfiguredFeature<?, ?>> var17 = var0.getOrThrow(OreFeatures.ORE_GOLD);
        Holder<ConfiguredFeature<?, ?>> var18 = var0.getOrThrow(OreFeatures.ORE_GOLD_BURIED);
        Holder<ConfiguredFeature<?, ?>> var19 = var0.getOrThrow(OreFeatures.ORE_REDSTONE);
        Holder<ConfiguredFeature<?, ?>> var20 = var0.getOrThrow(OreFeatures.ORE_DIAMOND_SMALL);
        Holder<ConfiguredFeature<?, ?>> var21 = var0.getOrThrow(OreFeatures.ORE_DIAMOND_LARGE);
        Holder<ConfiguredFeature<?, ?>> var22 = var0.getOrThrow(OreFeatures.ORE_DIAMOND_BURIED);
        Holder<ConfiguredFeature<?, ?>> var23 = var0.getOrThrow(OreFeatures.ORE_LAPIS);
        Holder<ConfiguredFeature<?, ?>> var24 = var0.getOrThrow(OreFeatures.ORE_LAPIS_BURIED);
        Holder<ConfiguredFeature<?, ?>> var25 = var0.getOrThrow(OreFeatures.ORE_INFESTED);
        Holder<ConfiguredFeature<?, ?>> var26 = var0.getOrThrow(OreFeatures.ORE_EMERALD);
        Holder<ConfiguredFeature<?, ?>> var27 = var0.getOrThrow(OreFeatures.ORE_ANCIENT_DEBRIS_LARGE);
        Holder<ConfiguredFeature<?, ?>> var28 = var0.getOrThrow(OreFeatures.ORE_ANCIENT_DEBRIS_SMALL);
        Holder<ConfiguredFeature<?, ?>> var29 = var0.getOrThrow(OreFeatures.ORE_COPPPER_SMALL);
        Holder<ConfiguredFeature<?, ?>> var30 = var0.getOrThrow(OreFeatures.ORE_COPPER_LARGE);
        Holder<ConfiguredFeature<?, ?>> var31 = var0.getOrThrow(OreFeatures.ORE_CLAY);
        PlacementUtils.register(
            param0, ORE_MAGMA, var1, commonOrePlacement(4, HeightRangePlacement.uniform(VerticalAnchor.absolute(27), VerticalAnchor.absolute(36)))
        );
        PlacementUtils.register(
            param0, ORE_SOUL_SAND, var2, commonOrePlacement(12, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(31)))
        );
        PlacementUtils.register(param0, ORE_GOLD_DELTAS, var3, commonOrePlacement(20, PlacementUtils.RANGE_10_10));
        PlacementUtils.register(param0, ORE_QUARTZ_DELTAS, var4, commonOrePlacement(32, PlacementUtils.RANGE_10_10));
        PlacementUtils.register(param0, ORE_GOLD_NETHER, var3, commonOrePlacement(10, PlacementUtils.RANGE_10_10));
        PlacementUtils.register(param0, ORE_QUARTZ_NETHER, var4, commonOrePlacement(16, PlacementUtils.RANGE_10_10));
        PlacementUtils.register(
            param0, ORE_GRAVEL_NETHER, var5, commonOrePlacement(2, HeightRangePlacement.uniform(VerticalAnchor.absolute(5), VerticalAnchor.absolute(41)))
        );
        PlacementUtils.register(
            param0, ORE_BLACKSTONE, var6, commonOrePlacement(2, HeightRangePlacement.uniform(VerticalAnchor.absolute(5), VerticalAnchor.absolute(31)))
        );
        PlacementUtils.register(
            param0, ORE_DIRT, var7, commonOrePlacement(7, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(160)))
        );
        PlacementUtils.register(param0, ORE_GRAVEL, var8, commonOrePlacement(14, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.top())));
        PlacementUtils.register(
            param0, ORE_GRANITE_UPPER, var9, rareOrePlacement(6, HeightRangePlacement.uniform(VerticalAnchor.absolute(64), VerticalAnchor.absolute(128)))
        );
        PlacementUtils.register(
            param0, ORE_GRANITE_LOWER, var9, commonOrePlacement(2, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(60)))
        );
        PlacementUtils.register(
            param0, ORE_DIORITE_UPPER, var10, rareOrePlacement(6, HeightRangePlacement.uniform(VerticalAnchor.absolute(64), VerticalAnchor.absolute(128)))
        );
        PlacementUtils.register(
            param0, ORE_DIORITE_LOWER, var10, commonOrePlacement(2, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(60)))
        );
        PlacementUtils.register(
            param0, ORE_ANDESITE_UPPER, var11, rareOrePlacement(6, HeightRangePlacement.uniform(VerticalAnchor.absolute(64), VerticalAnchor.absolute(128)))
        );
        PlacementUtils.register(
            param0, ORE_ANDESITE_LOWER, var11, commonOrePlacement(2, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(60)))
        );
        PlacementUtils.register(
            param0, ORE_TUFF, var12, commonOrePlacement(2, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(0)))
        );
        PlacementUtils.register(
            param0, ORE_COAL_UPPER, var13, commonOrePlacement(30, HeightRangePlacement.uniform(VerticalAnchor.absolute(136), VerticalAnchor.top()))
        );
        PlacementUtils.register(
            param0, ORE_COAL_LOWER, var14, commonOrePlacement(20, HeightRangePlacement.triangle(VerticalAnchor.absolute(0), VerticalAnchor.absolute(192)))
        );
        PlacementUtils.register(
            param0, ORE_IRON_UPPER, var15, commonOrePlacement(90, HeightRangePlacement.triangle(VerticalAnchor.absolute(80), VerticalAnchor.absolute(384)))
        );
        PlacementUtils.register(
            param0, ORE_IRON_MIDDLE, var15, commonOrePlacement(10, HeightRangePlacement.triangle(VerticalAnchor.absolute(-24), VerticalAnchor.absolute(56)))
        );
        PlacementUtils.register(
            param0, ORE_IRON_SMALL, var16, commonOrePlacement(10, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(72)))
        );
        PlacementUtils.register(
            param0, ORE_GOLD_EXTRA, var17, commonOrePlacement(50, HeightRangePlacement.uniform(VerticalAnchor.absolute(32), VerticalAnchor.absolute(256)))
        );
        PlacementUtils.register(
            param0, ORE_GOLD, var18, commonOrePlacement(4, HeightRangePlacement.triangle(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(32)))
        );
        PlacementUtils.register(
            param0,
            ORE_GOLD_LOWER,
            var18,
            orePlacement(CountPlacement.of(UniformInt.of(0, 1)), HeightRangePlacement.uniform(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(-48)))
        );
        PlacementUtils.register(
            param0, ORE_REDSTONE, var19, commonOrePlacement(4, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(15)))
        );
        PlacementUtils.register(
            param0,
            ORE_REDSTONE_LOWER,
            var19,
            commonOrePlacement(8, HeightRangePlacement.triangle(VerticalAnchor.aboveBottom(-32), VerticalAnchor.aboveBottom(32)))
        );
        PlacementUtils.register(
            param0, ORE_DIAMOND, var20, commonOrePlacement(7, HeightRangePlacement.triangle(VerticalAnchor.aboveBottom(-80), VerticalAnchor.aboveBottom(80)))
        );
        PlacementUtils.register(
            param0,
            ORE_DIAMOND_LARGE,
            var21,
            rareOrePlacement(9, HeightRangePlacement.triangle(VerticalAnchor.aboveBottom(-80), VerticalAnchor.aboveBottom(80)))
        );
        PlacementUtils.register(
            param0,
            ORE_DIAMOND_BURIED,
            var22,
            commonOrePlacement(4, HeightRangePlacement.triangle(VerticalAnchor.aboveBottom(-80), VerticalAnchor.aboveBottom(80)))
        );
        PlacementUtils.register(
            param0, ORE_LAPIS, var23, commonOrePlacement(2, HeightRangePlacement.triangle(VerticalAnchor.absolute(-32), VerticalAnchor.absolute(32)))
        );
        PlacementUtils.register(
            param0, ORE_LAPIS_BURIED, var24, commonOrePlacement(4, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(64)))
        );
        PlacementUtils.register(
            param0, ORE_INFESTED, var25, commonOrePlacement(14, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(63)))
        );
        PlacementUtils.register(
            param0, ORE_EMERALD, var26, commonOrePlacement(100, HeightRangePlacement.triangle(VerticalAnchor.absolute(-16), VerticalAnchor.absolute(480)))
        );
        PlacementUtils.register(
            param0,
            ORE_ANCIENT_DEBRIS_LARGE,
            var27,
            InSquarePlacement.spread(),
            HeightRangePlacement.triangle(VerticalAnchor.absolute(8), VerticalAnchor.absolute(24)),
            BiomeFilter.biome()
        );
        PlacementUtils.register(param0, ORE_ANCIENT_DEBRIS_SMALL, var28, InSquarePlacement.spread(), PlacementUtils.RANGE_8_8, BiomeFilter.biome());
        PlacementUtils.register(
            param0, ORE_COPPER, var29, commonOrePlacement(16, HeightRangePlacement.triangle(VerticalAnchor.absolute(-16), VerticalAnchor.absolute(112)))
        );
        PlacementUtils.register(
            param0, ORE_COPPER_LARGE, var30, commonOrePlacement(16, HeightRangePlacement.triangle(VerticalAnchor.absolute(-16), VerticalAnchor.absolute(112)))
        );
        PlacementUtils.register(param0, ORE_CLAY, var31, commonOrePlacement(46, PlacementUtils.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT));
    }
}

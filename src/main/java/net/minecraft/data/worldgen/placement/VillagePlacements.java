package net.minecraft.data.worldgen.placement;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.features.PileFeatures;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class VillagePlacements {
    public static final ResourceKey<PlacedFeature> PILE_HAY_VILLAGE = PlacementUtils.createKey("pile_hay");
    public static final ResourceKey<PlacedFeature> PILE_MELON_VILLAGE = PlacementUtils.createKey("pile_melon");
    public static final ResourceKey<PlacedFeature> PILE_SNOW_VILLAGE = PlacementUtils.createKey("pile_snow");
    public static final ResourceKey<PlacedFeature> PILE_ICE_VILLAGE = PlacementUtils.createKey("pile_ice");
    public static final ResourceKey<PlacedFeature> PILE_PUMPKIN_VILLAGE = PlacementUtils.createKey("pile_pumpkin");
    public static final ResourceKey<PlacedFeature> OAK_VILLAGE = PlacementUtils.createKey("oak");
    public static final ResourceKey<PlacedFeature> ACACIA_VILLAGE = PlacementUtils.createKey("acacia");
    public static final ResourceKey<PlacedFeature> SPRUCE_VILLAGE = PlacementUtils.createKey("spruce");
    public static final ResourceKey<PlacedFeature> PINE_VILLAGE = PlacementUtils.createKey("pine");
    public static final ResourceKey<PlacedFeature> PATCH_CACTUS_VILLAGE = PlacementUtils.createKey("patch_cactus");
    public static final ResourceKey<PlacedFeature> FLOWER_PLAIN_VILLAGE = PlacementUtils.createKey("flower_plain");
    public static final ResourceKey<PlacedFeature> PATCH_TAIGA_GRASS_VILLAGE = PlacementUtils.createKey("patch_taiga_grass");
    public static final ResourceKey<PlacedFeature> PATCH_BERRY_BUSH_VILLAGE = PlacementUtils.createKey("patch_berry_bush");

    public static void bootstrap(BootstapContext<PlacedFeature> param0) {
        HolderGetter<ConfiguredFeature<?, ?>> var0 = param0.lookup(Registry.CONFIGURED_FEATURE_REGISTRY);
        Holder<ConfiguredFeature<?, ?>> var1 = var0.getOrThrow(PileFeatures.PILE_HAY);
        Holder<ConfiguredFeature<?, ?>> var2 = var0.getOrThrow(PileFeatures.PILE_MELON);
        Holder<ConfiguredFeature<?, ?>> var3 = var0.getOrThrow(PileFeatures.PILE_SNOW);
        Holder<ConfiguredFeature<?, ?>> var4 = var0.getOrThrow(PileFeatures.PILE_ICE);
        Holder<ConfiguredFeature<?, ?>> var5 = var0.getOrThrow(PileFeatures.PILE_PUMPKIN);
        Holder<ConfiguredFeature<?, ?>> var6 = var0.getOrThrow(TreeFeatures.OAK);
        Holder<ConfiguredFeature<?, ?>> var7 = var0.getOrThrow(TreeFeatures.ACACIA);
        Holder<ConfiguredFeature<?, ?>> var8 = var0.getOrThrow(TreeFeatures.SPRUCE);
        Holder<ConfiguredFeature<?, ?>> var9 = var0.getOrThrow(TreeFeatures.PINE);
        Holder<ConfiguredFeature<?, ?>> var10 = var0.getOrThrow(VegetationFeatures.PATCH_CACTUS);
        Holder<ConfiguredFeature<?, ?>> var11 = var0.getOrThrow(VegetationFeatures.FLOWER_PLAIN);
        Holder<ConfiguredFeature<?, ?>> var12 = var0.getOrThrow(VegetationFeatures.PATCH_TAIGA_GRASS);
        Holder<ConfiguredFeature<?, ?>> var13 = var0.getOrThrow(VegetationFeatures.PATCH_BERRY_BUSH);
        PlacementUtils.register(param0, PILE_HAY_VILLAGE, var1);
        PlacementUtils.register(param0, PILE_MELON_VILLAGE, var2);
        PlacementUtils.register(param0, PILE_SNOW_VILLAGE, var3);
        PlacementUtils.register(param0, PILE_ICE_VILLAGE, var4);
        PlacementUtils.register(param0, PILE_PUMPKIN_VILLAGE, var5);
        PlacementUtils.register(param0, OAK_VILLAGE, var6, PlacementUtils.filteredByBlockSurvival(Blocks.OAK_SAPLING));
        PlacementUtils.register(param0, ACACIA_VILLAGE, var7, PlacementUtils.filteredByBlockSurvival(Blocks.ACACIA_SAPLING));
        PlacementUtils.register(param0, SPRUCE_VILLAGE, var8, PlacementUtils.filteredByBlockSurvival(Blocks.SPRUCE_SAPLING));
        PlacementUtils.register(param0, PINE_VILLAGE, var9, PlacementUtils.filteredByBlockSurvival(Blocks.SPRUCE_SAPLING));
        PlacementUtils.register(param0, PATCH_CACTUS_VILLAGE, var10);
        PlacementUtils.register(param0, FLOWER_PLAIN_VILLAGE, var11);
        PlacementUtils.register(param0, PATCH_TAIGA_GRASS_VILLAGE, var12);
        PlacementUtils.register(param0, PATCH_BERRY_BUSH_VILLAGE, var13);
    }
}

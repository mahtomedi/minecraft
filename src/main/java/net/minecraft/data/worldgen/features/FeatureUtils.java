package net.minecraft.data.worldgen.features;

import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class FeatureUtils {
    public static void bootstrap(BootstapContext<ConfiguredFeature<?, ?>> param0) {
        AquaticFeatures.bootstrap(param0);
        CaveFeatures.bootstrap(param0);
        EndFeatures.bootstrap(param0);
        MiscOverworldFeatures.bootstrap(param0);
        NetherFeatures.bootstrap(param0);
        OreFeatures.bootstrap(param0);
        PileFeatures.bootstrap(param0);
        TreeFeatures.bootstrap(param0);
        VegetationFeatures.bootstrap(param0);
    }

    private static BlockPredicate simplePatchPredicate(List<Block> param0) {
        BlockPredicate var0;
        if (!param0.isEmpty()) {
            var0 = BlockPredicate.allOf(BlockPredicate.ONLY_IN_AIR_PREDICATE, BlockPredicate.matchesBlocks(Direction.DOWN.getNormal(), param0));
        } else {
            var0 = BlockPredicate.ONLY_IN_AIR_PREDICATE;
        }

        return var0;
    }

    public static RandomPatchConfiguration simpleRandomPatchConfiguration(int param0, Holder<PlacedFeature> param1) {
        return new RandomPatchConfiguration(param0, 7, 3, param1);
    }

    public static <FC extends FeatureConfiguration, F extends Feature<FC>> RandomPatchConfiguration simplePatchConfiguration(
        F param0, FC param1, List<Block> param2, int param3
    ) {
        return simpleRandomPatchConfiguration(param3, PlacementUtils.filtered(param0, param1, simplePatchPredicate(param2)));
    }

    public static <FC extends FeatureConfiguration, F extends Feature<FC>> RandomPatchConfiguration simplePatchConfiguration(
        F param0, FC param1, List<Block> param2
    ) {
        return simplePatchConfiguration(param0, param1, param2, 96);
    }

    public static <FC extends FeatureConfiguration, F extends Feature<FC>> RandomPatchConfiguration simplePatchConfiguration(F param0, FC param1) {
        return simplePatchConfiguration(param0, param1, List.of(), 96);
    }

    public static ResourceKey<ConfiguredFeature<?, ?>> createKey(String param0) {
        return ResourceKey.create(Registry.CONFIGURED_FEATURE_REGISTRY, new ResourceLocation(param0));
    }

    public static void register(
        BootstapContext<ConfiguredFeature<?, ?>> param0, ResourceKey<ConfiguredFeature<?, ?>> param1, Feature<NoneFeatureConfiguration> param2
    ) {
        register(param0, param1, param2, FeatureConfiguration.NONE);
    }

    public static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(
        BootstapContext<ConfiguredFeature<?, ?>> param0, ResourceKey<ConfiguredFeature<?, ?>> param1, F param2, FC param3
    ) {
        param0.register(param1, new ConfiguredFeature(param2, param3));
    }
}

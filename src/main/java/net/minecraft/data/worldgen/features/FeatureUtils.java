package net.minecraft.data.worldgen.features;

import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class FeatureUtils {
    public static Holder<? extends ConfiguredFeature<?, ?>> bootstrap(Registry<ConfiguredFeature<?, ?>> param0) {
        List<Holder<? extends ConfiguredFeature<?, ?>>> var0 = List.of(
            AquaticFeatures.KELP,
            CaveFeatures.MOSS_PATCH_BONEMEAL,
            EndFeatures.CHORUS_PLANT,
            MiscOverworldFeatures.SPRING_LAVA_OVERWORLD,
            NetherFeatures.BASALT_BLOBS,
            OreFeatures.ORE_ANCIENT_DEBRIS_LARGE,
            PileFeatures.PILE_HAY,
            TreeFeatures.AZALEA_TREE,
            VegetationFeatures.TREES_OLD_GROWTH_PINE_TAIGA
        );
        return Util.getRandom(var0, RandomSource.create());
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

    public static Holder<ConfiguredFeature<NoneFeatureConfiguration, ?>> register(String param0, Feature<NoneFeatureConfiguration> param1) {
        return register(param0, param1, FeatureConfiguration.NONE);
    }

    public static <FC extends FeatureConfiguration, F extends Feature<FC>> Holder<ConfiguredFeature<FC, ?>> register(String param0, F param1, FC param2) {
        return BuiltinRegistries.registerExact(BuiltinRegistries.CONFIGURED_FEATURE, param0, new ConfiguredFeature<>(param1, param2));
    }
}

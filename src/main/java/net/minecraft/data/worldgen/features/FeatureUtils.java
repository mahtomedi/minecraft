package net.minecraft.data.worldgen.features;

import java.util.List;
import java.util.Random;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class FeatureUtils {
    public static ConfiguredFeature<?, ?> bootstrap() {
        ConfiguredFeature<?, ?>[] var0 = new ConfiguredFeature[]{
            AquaticFeatures.KELP,
            CaveFeatures.MOSS_PATCH_BONEMEAL,
            EndFeatures.CHORUS_PLANT,
            MiscOverworldFeatures.SPRING_LAVA_OVERWORLD,
            NetherFeatures.BASALT_BLOBS,
            OreFeatures.ORE_ANCIENT_DEBRIS_LARGE,
            PileFeatures.PILE_HAY,
            TreeFeatures.AZALEA_TREE,
            VegetationFeatures.TREES_OLD_GROWTH_PINE_TAIGA
        };
        return Util.getRandom(var0, new Random());
    }

    private static BlockPredicate simplePatchPredicate(List<Block> param0) {
        BlockPredicate var0;
        if (!param0.isEmpty()) {
            var0 = BlockPredicate.allOf(BlockPredicate.ONLY_IN_AIR_PREDICATE, BlockPredicate.matchesBlocks(param0, new BlockPos(0, -1, 0)));
        } else {
            var0 = BlockPredicate.ONLY_IN_AIR_PREDICATE;
        }

        return var0;
    }

    public static RandomPatchConfiguration simpleRandomPatchConfiguration(int param0, PlacedFeature param1) {
        return new RandomPatchConfiguration(param0, 7, 3, () -> param1);
    }

    public static RandomPatchConfiguration simplePatchConfiguration(ConfiguredFeature<?, ?> param0, List<Block> param1, int param2) {
        return simpleRandomPatchConfiguration(param2, param0.filtered(simplePatchPredicate(param1)));
    }

    public static RandomPatchConfiguration simplePatchConfiguration(ConfiguredFeature<?, ?> param0, List<Block> param1) {
        return simplePatchConfiguration(param0, param1, 96);
    }

    public static RandomPatchConfiguration simplePatchConfiguration(ConfiguredFeature<?, ?> param0) {
        return simplePatchConfiguration(param0, List.of(), 96);
    }

    public static <FC extends FeatureConfiguration> ConfiguredFeature<FC, ?> register(String param0, ConfiguredFeature<FC, ?> param1) {
        return Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, param0, param1);
    }
}

package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConfiguredFeature<FC extends FeatureConfiguration, F extends Feature<FC>> {
    public static final Codec<ConfiguredFeature<?, ?>> DIRECT_CODEC = Registry.FEATURE
        .byNameCodec()
        .dispatch(param0 -> param0.feature, Feature::configuredCodec);
    public static final Codec<Supplier<ConfiguredFeature<?, ?>>> CODEC = RegistryFileCodec.create(Registry.CONFIGURED_FEATURE_REGISTRY, DIRECT_CODEC);
    public static final Codec<List<Supplier<ConfiguredFeature<?, ?>>>> LIST_CODEC = RegistryFileCodec.homogeneousList(
        Registry.CONFIGURED_FEATURE_REGISTRY, DIRECT_CODEC
    );
    public static final Logger LOGGER = LogManager.getLogger();
    public final F feature;
    public final FC config;

    public ConfiguredFeature(F param0, FC param1) {
        this.feature = param0;
        this.config = param1;
    }

    public F feature() {
        return this.feature;
    }

    public FC config() {
        return this.config;
    }

    public PlacedFeature placed(List<PlacementModifier> param0) {
        return new PlacedFeature(() -> this, param0);
    }

    public PlacedFeature placed(PlacementModifier... param0) {
        return this.placed(List.of(param0));
    }

    public PlacedFeature filteredByBlockSurvival(Block param0) {
        return this.filtered(BlockPredicate.wouldSurvive(param0.defaultBlockState(), BlockPos.ZERO));
    }

    public PlacedFeature onlyWhenEmpty() {
        return this.filtered(BlockPredicate.matchesBlock(Blocks.AIR, BlockPos.ZERO));
    }

    public PlacedFeature filtered(BlockPredicate param0) {
        return this.placed(BlockPredicateFilter.forPredicate(param0));
    }

    public boolean place(WorldGenLevel param0, ChunkGenerator param1, Random param2, BlockPos param3) {
        return param0.ensureCanWrite(param3)
            ? this.feature.place(new FeaturePlaceContext<>(Optional.empty(), param0, param1, param2, param3, this.config))
            : false;
    }

    public Stream<ConfiguredFeature<?, ?>> getFeatures() {
        return Stream.concat(Stream.of(this), this.config.getFeatures());
    }

    @Override
    public String toString() {
        return BuiltinRegistries.CONFIGURED_FEATURE
            .getResourceKey(this)
            .map(Objects::toString)
            .orElseGet(() -> DIRECT_CODEC.encodeStart(JsonOps.INSTANCE, this).toString());
    }
}

package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratedFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.ConfiguredDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConfiguredFeature<FC extends FeatureConfiguration, F extends Feature<FC>> {
    public static final ConfiguredFeature<?, ?> NOPE = new ConfiguredFeature<>(Feature.NO_OP, NoneFeatureConfiguration.NONE);
    public static final Codec<ConfiguredFeature<?, ?>> CODEC = Registry.FEATURE
        .<ConfiguredFeature<?, ?>>dispatch("name", param0 -> param0.feature, Feature::configuredCodec)
        .withDefault(NOPE);
    public static final Logger LOGGER = LogManager.getLogger();
    public final F feature;
    public final FC config;

    public ConfiguredFeature(F param0, FC param1) {
        this.feature = param0;
        this.config = param1;
    }

    public ConfiguredFeature<?, ?> decorated(ConfiguredDecorator<?> param0) {
        Feature<DecoratedFeatureConfiguration> var0 = this.feature instanceof AbstractFlowerFeature ? Feature.DECORATED_FLOWER : Feature.DECORATED;
        return var0.configured(new DecoratedFeatureConfiguration(this, param0));
    }

    public WeightedConfiguredFeature<FC> weighted(float param0) {
        return new WeightedConfiguredFeature<>(this, param0);
    }

    public boolean place(WorldGenLevel param0, ChunkGenerator param1, Random param2, BlockPos param3) {
        return this.feature.place(param0, param1, param2, param3, this.config);
    }
}

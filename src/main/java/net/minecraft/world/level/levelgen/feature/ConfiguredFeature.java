package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratedFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.ConfiguredDecorator;

public class ConfiguredFeature<FC extends FeatureConfiguration, F extends Feature<FC>> {
    public final F feature;
    public final FC config;

    public ConfiguredFeature(F param0, FC param1) {
        this.feature = param0;
        this.config = param1;
    }

    public ConfiguredFeature(F param0, Dynamic<?> param1) {
        this(param0, param0.createSettings(param1));
    }

    public ConfiguredFeature<?, ?> decorated(ConfiguredDecorator<?> param0) {
        Feature<DecoratedFeatureConfiguration> var0 = this.feature instanceof AbstractFlowerFeature ? Feature.DECORATED_FLOWER : Feature.DECORATED;
        return var0.configured(new DecoratedFeatureConfiguration(this, param0));
    }

    public WeightedConfiguredFeature<FC> weighted(float param0) {
        return new WeightedConfiguredFeature<>(this, param0);
    }

    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.of(
                    param0.createString("name"),
                    param0.createString(Registry.FEATURE.getKey(this.feature).toString()),
                    param0.createString("config"),
                    this.config.serialize(param0).getValue()
                )
            )
        );
    }

    public boolean place(LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3) {
        return this.feature.place(param0, param1, param2, param3, this.config);
    }

    public static <T> ConfiguredFeature<?, ?> deserialize(Dynamic<T> param0) {
        Feature<? extends FeatureConfiguration> var0 = (Feature)Registry.FEATURE.get(new ResourceLocation(param0.get("name").asString("")));
        return new ConfiguredFeature<>(var0, param0.get("config").orElseEmptyMap());
    }
}

package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class WeightedConfiguredFeature<FC extends FeatureConfiguration> {
    public final ConfiguredFeature<FC, ?> feature;
    public final float chance;

    public WeightedConfiguredFeature(ConfiguredFeature<FC, ?> param0, float param1) {
        this.feature = param0;
        this.chance = param1;
    }

    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.of(
                    param0.createString("name"),
                    param0.createString(Registry.FEATURE.getKey(this.feature.feature).toString()),
                    param0.createString("config"),
                    this.feature.config.serialize(param0).getValue(),
                    param0.createString("chance"),
                    param0.createFloat(this.chance)
                )
            )
        );
    }

    public boolean place(LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3) {
        return this.feature.place(param0, param1, param2, param3);
    }

    public static <T> WeightedConfiguredFeature<?> deserialize(Dynamic<T> param0) {
        return ConfiguredFeature.deserialize(param0).weighted(param0.get("chance").asFloat(0.0F));
    }
}

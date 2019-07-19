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

public class WeightedConfiguredFeature<FC extends FeatureConfiguration> {
    public final Feature<FC> feature;
    public final FC config;
    public final Float chance;

    public WeightedConfiguredFeature(Feature<FC> param0, FC param1, Float param2) {
        this.feature = param0;
        this.config = param1;
        this.chance = param2;
    }

    public WeightedConfiguredFeature(Feature<FC> param0, Dynamic<?> param1, float param2) {
        this(param0, param0.createSettings(param1), Float.valueOf(param2));
    }

    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.of(
                    param0.createString("name"),
                    param0.createString(Registry.FEATURE.getKey(this.feature).toString()),
                    param0.createString("config"),
                    this.config.serialize(param0).getValue(),
                    param0.createString("chance"),
                    param0.createFloat(this.chance)
                )
            )
        );
    }

    public boolean place(LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3) {
        return this.feature.place(param0, param1, param2, param3, this.config);
    }

    public static <T> WeightedConfiguredFeature<?> deserialize(Dynamic<T> param0) {
        Feature<? extends FeatureConfiguration> var0 = (Feature)Registry.FEATURE.get(new ResourceLocation(param0.get("name").asString("")));
        return new WeightedConfiguredFeature<>(var0, param0.get("config").orElseEmptyMap(), param0.get("chance").asFloat(0.0F));
    }
}

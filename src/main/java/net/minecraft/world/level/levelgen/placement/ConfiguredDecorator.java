package net.minecraft.world.level.levelgen.placement;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class ConfiguredDecorator<DC extends DecoratorConfiguration> {
    public final FeatureDecorator<DC> decorator;
    public final DC config;

    public ConfiguredDecorator(FeatureDecorator<DC> param0, Dynamic<?> param1) {
        this(param0, param0.createSettings(param1));
    }

    public ConfiguredDecorator(FeatureDecorator<DC> param0, DC param1) {
        this.decorator = param0;
        this.config = param1;
    }

    public <FC extends FeatureConfiguration, F extends Feature<FC>> boolean place(
        LevelAccessor param0,
        StructureFeatureManager param1,
        ChunkGenerator<? extends ChunkGeneratorSettings> param2,
        Random param3,
        BlockPos param4,
        ConfiguredFeature<FC, F> param5
    ) {
        return this.decorator.placeFeature(param0, param1, param2, param3, param4, this.config, param5);
    }

    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.of(
                    param0.createString("name"),
                    param0.createString(Registry.DECORATOR.getKey(this.decorator).toString()),
                    param0.createString("config"),
                    this.config.serialize(param0).getValue()
                )
            )
        );
    }

    public static <T> ConfiguredDecorator<?> deserialize(Dynamic<T> param0) {
        FeatureDecorator<? extends DecoratorConfiguration> var0 = (FeatureDecorator)Registry.DECORATOR
            .get(new ResourceLocation(param0.get("name").asString("")));
        return new ConfiguredDecorator<>(var0, param0.get("config").orElseEmptyMap());
    }
}

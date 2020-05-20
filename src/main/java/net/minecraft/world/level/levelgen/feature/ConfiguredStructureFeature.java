package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class ConfiguredStructureFeature<FC extends FeatureConfiguration, F extends StructureFeature<FC>> {
    public static final Codec<ConfiguredStructureFeature<?, ?>> CODEC = Registry.STRUCTURE_FEATURE
        .dispatch("name", param0 -> param0.feature, StructureFeature::configuredStructureCodec);
    public final F feature;
    public final FC config;

    public ConfiguredStructureFeature(F param0, FC param1) {
        this.feature = param0;
        this.config = param1;
    }

    public StructureStart<?> generate(
        ChunkGenerator param0,
        BiomeSource param1,
        StructureManager param2,
        long param3,
        ChunkPos param4,
        Biome param5,
        int param6,
        StructureFeatureConfiguration param7
    ) {
        return this.feature.generate(param0, param1, param2, param3, param4, param5, param6, new WorldgenRandom(), param7, this.config);
    }
}

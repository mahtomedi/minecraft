package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class ConfiguredStructureFeature<FC extends FeatureConfiguration, F extends StructureFeature<FC>> {
    public static final Codec<ConfiguredStructureFeature<?, ?>> DIRECT_CODEC = Registry.STRUCTURE_FEATURE
        .byNameCodec()
        .dispatch(param0 -> param0.feature, StructureFeature::configuredStructureCodec);
    public static final Codec<Holder<ConfiguredStructureFeature<?, ?>>> CODEC = RegistryFileCodec.create(
        Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, DIRECT_CODEC
    );
    public static final Codec<HolderSet<ConfiguredStructureFeature<?, ?>>> LIST_CODEC = RegistryCodecs.homogeneousList(
        Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, DIRECT_CODEC
    );
    public final F feature;
    public final FC config;
    public final HolderSet<Biome> biomes;

    public ConfiguredStructureFeature(F param0, FC param1, HolderSet<Biome> param2) {
        this.feature = param0;
        this.config = param1;
        this.biomes = param2;
    }

    public StructureStart<?> generate(
        RegistryAccess param0,
        ChunkGenerator param1,
        BiomeSource param2,
        StructureManager param3,
        long param4,
        ChunkPos param5,
        int param6,
        LevelHeightAccessor param7,
        Predicate<Holder<Biome>> param8
    ) {
        return this.feature.generate(param0, param1, param2, param3, param4, param5, param6, this.config, param7, param8);
    }

    public HolderSet<Biome> biomes() {
        return this.biomes;
    }
}

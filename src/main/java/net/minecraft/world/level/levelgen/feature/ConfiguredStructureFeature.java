package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
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
    public final Map<MobCategory, StructureSpawnOverride> spawnOverrides;
    public final boolean adaptNoise;

    public ConfiguredStructureFeature(F param0, FC param1, HolderSet<Biome> param2, boolean param3, Map<MobCategory, StructureSpawnOverride> param4) {
        this.feature = param0;
        this.config = param1;
        this.biomes = param2;
        this.adaptNoise = param3;
        this.spawnOverrides = param4;
    }

    public StructureStart generate(
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
        Optional<PieceGenerator<FC>> var0 = this.feature
            .pieceGeneratorSupplier()
            .createGenerator(new PieceGeneratorSupplier.Context<>(param1, param2, param4, param5, this.config, param7, param8, param3, param0));
        if (var0.isPresent()) {
            StructurePiecesBuilder var1 = new StructurePiecesBuilder();
            WorldgenRandom var2 = new WorldgenRandom(new LegacyRandomSource(0L));
            var2.setLargeFeatureSeed(param4, param5.x, param5.z);
            var0.get().generatePieces(var1, new PieceGenerator.Context<>(this.config, param1, param3, param5, param7, var2, param4));
            StructureStart var3 = new StructureStart(this, param5, param6, var1.build());
            if (var3.isValid()) {
                return var3;
            }
        }

        return StructureStart.INVALID_START;
    }

    public HolderSet<Biome> biomes() {
        return this.biomes;
    }

    public BoundingBox adjustBoundingBox(BoundingBox param0) {
        return this.adaptNoise ? param0.inflatedBy(12) : param0;
    }
}

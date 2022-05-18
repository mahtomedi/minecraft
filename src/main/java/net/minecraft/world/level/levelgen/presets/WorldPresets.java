package net.minecraft.world.level.levelgen.presets;

import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class WorldPresets {
    public static final ResourceKey<WorldPreset> NORMAL = register("normal");
    public static final ResourceKey<WorldPreset> FLAT = register("flat");
    public static final ResourceKey<WorldPreset> LARGE_BIOMES = register("large_biomes");
    public static final ResourceKey<WorldPreset> AMPLIFIED = register("amplified");
    public static final ResourceKey<WorldPreset> SINGLE_BIOME_SURFACE = register("single_biome_surface");
    public static final ResourceKey<WorldPreset> DEBUG = register("debug_all_block_states");

    public static Holder<WorldPreset> bootstrap(Registry<WorldPreset> param0) {
        return new WorldPresets.Bootstrap(param0).run();
    }

    private static ResourceKey<WorldPreset> register(String param0) {
        return ResourceKey.create(Registry.WORLD_PRESET_REGISTRY, new ResourceLocation(param0));
    }

    public static Optional<ResourceKey<WorldPreset>> fromSettings(WorldGenSettings param0) {
        ChunkGenerator var0 = param0.overworld();
        if (var0 instanceof FlatLevelSource) {
            return Optional.of(FLAT);
        } else {
            return var0 instanceof DebugLevelSource ? Optional.of(DEBUG) : Optional.empty();
        }
    }

    public static WorldGenSettings createNormalWorldFromPreset(RegistryAccess param0, long param1, boolean param2, boolean param3) {
        return param0.registryOrThrow(Registry.WORLD_PRESET_REGISTRY).getHolderOrThrow(NORMAL).value().createWorldGenSettings(param1, param2, param3);
    }

    public static WorldGenSettings createNormalWorldFromPreset(RegistryAccess param0, long param1) {
        return createNormalWorldFromPreset(param0, param1, true, false);
    }

    public static WorldGenSettings createNormalWorldFromPreset(RegistryAccess param0) {
        return createNormalWorldFromPreset(param0, RandomSource.create().nextLong());
    }

    public static WorldGenSettings demoSettings(RegistryAccess param0) {
        return createNormalWorldFromPreset(param0, (long)"North Carolina".hashCode(), true, true);
    }

    public static LevelStem getNormalOverworld(RegistryAccess param0) {
        return param0.registryOrThrow(Registry.WORLD_PRESET_REGISTRY).getHolderOrThrow(NORMAL).value().overworldOrThrow();
    }

    static class Bootstrap {
        private final Registry<WorldPreset> presets;
        private final Registry<DimensionType> dimensionTypes = BuiltinRegistries.DIMENSION_TYPE;
        private final Registry<Biome> biomes = BuiltinRegistries.BIOME;
        private final Registry<StructureSet> structureSets = BuiltinRegistries.STRUCTURE_SETS;
        private final Registry<NoiseGeneratorSettings> noiseSettings = BuiltinRegistries.NOISE_GENERATOR_SETTINGS;
        private final Registry<NormalNoise.NoiseParameters> noises = BuiltinRegistries.NOISE;
        private final Holder<DimensionType> overworldDimensionType = this.dimensionTypes.getOrCreateHolderOrThrow(BuiltinDimensionTypes.OVERWORLD);
        private final Holder<DimensionType> netherDimensionType = this.dimensionTypes.getOrCreateHolderOrThrow(BuiltinDimensionTypes.NETHER);
        private final Holder<NoiseGeneratorSettings> netherNoiseSettings = this.noiseSettings.getOrCreateHolderOrThrow(NoiseGeneratorSettings.NETHER);
        private final LevelStem netherStem = new LevelStem(
            this.netherDimensionType,
            new NoiseBasedChunkGenerator(
                this.structureSets, this.noises, MultiNoiseBiomeSource.Preset.NETHER.biomeSource(this.biomes), this.netherNoiseSettings
            )
        );
        private final Holder<DimensionType> endDimensionType = this.dimensionTypes.getOrCreateHolderOrThrow(BuiltinDimensionTypes.END);
        private final Holder<NoiseGeneratorSettings> endNoiseSettings = this.noiseSettings.getOrCreateHolderOrThrow(NoiseGeneratorSettings.END);
        private final LevelStem endStem = new LevelStem(
            this.endDimensionType, new NoiseBasedChunkGenerator(this.structureSets, this.noises, new TheEndBiomeSource(this.biomes), this.endNoiseSettings)
        );

        Bootstrap(Registry<WorldPreset> param0) {
            this.presets = param0;
        }

        private LevelStem makeOverworld(ChunkGenerator param0) {
            return new LevelStem(this.overworldDimensionType, param0);
        }

        private LevelStem makeNoiseBasedOverworld(BiomeSource param0, Holder<NoiseGeneratorSettings> param1) {
            return this.makeOverworld(new NoiseBasedChunkGenerator(this.structureSets, this.noises, param0, param1));
        }

        private WorldPreset createPresetWithCustomOverworld(LevelStem param0) {
            return new WorldPreset(Map.of(LevelStem.OVERWORLD, param0, LevelStem.NETHER, this.netherStem, LevelStem.END, this.endStem));
        }

        private Holder<WorldPreset> registerCustomOverworldPreset(ResourceKey<WorldPreset> param0, LevelStem param1) {
            return BuiltinRegistries.register(this.presets, param0, this.createPresetWithCustomOverworld(param1));
        }

        public Holder<WorldPreset> run() {
            MultiNoiseBiomeSource var0 = MultiNoiseBiomeSource.Preset.OVERWORLD.biomeSource(this.biomes);
            Holder<NoiseGeneratorSettings> var1 = this.noiseSettings.getOrCreateHolderOrThrow(NoiseGeneratorSettings.OVERWORLD);
            this.registerCustomOverworldPreset(WorldPresets.NORMAL, this.makeNoiseBasedOverworld(var0, var1));
            Holder<NoiseGeneratorSettings> var2 = this.noiseSettings.getOrCreateHolderOrThrow(NoiseGeneratorSettings.LARGE_BIOMES);
            this.registerCustomOverworldPreset(WorldPresets.LARGE_BIOMES, this.makeNoiseBasedOverworld(var0, var2));
            Holder<NoiseGeneratorSettings> var3 = this.noiseSettings.getOrCreateHolderOrThrow(NoiseGeneratorSettings.AMPLIFIED);
            this.registerCustomOverworldPreset(WorldPresets.AMPLIFIED, this.makeNoiseBasedOverworld(var0, var3));
            this.registerCustomOverworldPreset(
                WorldPresets.SINGLE_BIOME_SURFACE,
                this.makeNoiseBasedOverworld(new FixedBiomeSource(this.biomes.getOrCreateHolderOrThrow(Biomes.PLAINS)), var1)
            );
            this.registerCustomOverworldPreset(
                WorldPresets.FLAT,
                this.makeOverworld(new FlatLevelSource(this.structureSets, FlatLevelGeneratorSettings.getDefault(this.biomes, this.structureSets)))
            );
            return this.registerCustomOverworldPreset(WorldPresets.DEBUG, this.makeOverworld(new DebugLevelSource(this.structureSets, this.biomes)));
        }
    }
}

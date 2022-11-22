package net.minecraft.world.level.levelgen.presets;

import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.StructureSet;

public class WorldPresets {
    public static final ResourceKey<WorldPreset> NORMAL = register("normal");
    public static final ResourceKey<WorldPreset> FLAT = register("flat");
    public static final ResourceKey<WorldPreset> LARGE_BIOMES = register("large_biomes");
    public static final ResourceKey<WorldPreset> AMPLIFIED = register("amplified");
    public static final ResourceKey<WorldPreset> SINGLE_BIOME_SURFACE = register("single_biome_surface");
    public static final ResourceKey<WorldPreset> DEBUG = register("debug_all_block_states");

    public static void bootstrap(BootstapContext<WorldPreset> param0) {
        new WorldPresets.Bootstrap(param0).run();
    }

    private static ResourceKey<WorldPreset> register(String param0) {
        return ResourceKey.create(Registries.WORLD_PRESET, new ResourceLocation(param0));
    }

    public static Optional<ResourceKey<WorldPreset>> fromSettings(Registry<LevelStem> param0) {
        return param0.getOptional(LevelStem.OVERWORLD).flatMap(param0x -> {
            ChunkGenerator var0x = param0x.generator();
            if (var0x instanceof FlatLevelSource) {
                return Optional.of(FLAT);
            } else {
                return var0x instanceof DebugLevelSource ? Optional.of(DEBUG) : Optional.empty();
            }
        });
    }

    public static WorldDimensions createNormalWorldDimensions(RegistryAccess param0) {
        return param0.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(NORMAL).value().createWorldDimensions();
    }

    public static LevelStem getNormalOverworld(RegistryAccess param0) {
        return (LevelStem)param0.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(NORMAL).value().overworld().orElseThrow();
    }

    static class Bootstrap {
        private final BootstapContext<WorldPreset> context;
        private final HolderGetter<NoiseGeneratorSettings> noiseSettings;
        private final HolderGetter<Biome> biomes;
        private final HolderGetter<PlacedFeature> placedFeatures;
        private final HolderGetter<StructureSet> structureSets;
        private final Holder<DimensionType> overworldDimensionType;
        private final LevelStem netherStem;
        private final LevelStem endStem;

        Bootstrap(BootstapContext<WorldPreset> param0) {
            this.context = param0;
            HolderGetter<DimensionType> var0 = param0.lookup(Registries.DIMENSION_TYPE);
            this.noiseSettings = param0.lookup(Registries.NOISE_SETTINGS);
            this.biomes = param0.lookup(Registries.BIOME);
            this.placedFeatures = param0.lookup(Registries.PLACED_FEATURE);
            this.structureSets = param0.lookup(Registries.STRUCTURE_SET);
            this.overworldDimensionType = var0.getOrThrow(BuiltinDimensionTypes.OVERWORLD);
            Holder<DimensionType> var1 = var0.getOrThrow(BuiltinDimensionTypes.NETHER);
            Holder<NoiseGeneratorSettings> var2 = this.noiseSettings.getOrThrow(NoiseGeneratorSettings.NETHER);
            this.netherStem = new LevelStem(var1, new NoiseBasedChunkGenerator(MultiNoiseBiomeSource.Preset.NETHER.biomeSource(this.biomes), var2));
            Holder<DimensionType> var3 = var0.getOrThrow(BuiltinDimensionTypes.END);
            Holder<NoiseGeneratorSettings> var4 = this.noiseSettings.getOrThrow(NoiseGeneratorSettings.END);
            this.endStem = new LevelStem(var3, new NoiseBasedChunkGenerator(TheEndBiomeSource.create(this.biomes), var4));
        }

        private LevelStem makeOverworld(ChunkGenerator param0) {
            return new LevelStem(this.overworldDimensionType, param0);
        }

        private LevelStem makeNoiseBasedOverworld(BiomeSource param0, Holder<NoiseGeneratorSettings> param1) {
            return this.makeOverworld(new NoiseBasedChunkGenerator(param0, param1));
        }

        private WorldPreset createPresetWithCustomOverworld(LevelStem param0) {
            return new WorldPreset(Map.of(LevelStem.OVERWORLD, param0, LevelStem.NETHER, this.netherStem, LevelStem.END, this.endStem));
        }

        private void registerCustomOverworldPreset(ResourceKey<WorldPreset> param0, LevelStem param1) {
            this.context.register(param0, this.createPresetWithCustomOverworld(param1));
        }

        public void run() {
            MultiNoiseBiomeSource var0 = MultiNoiseBiomeSource.Preset.OVERWORLD.biomeSource(this.biomes);
            Holder<NoiseGeneratorSettings> var1 = this.noiseSettings.getOrThrow(NoiseGeneratorSettings.OVERWORLD);
            this.registerCustomOverworldPreset(WorldPresets.NORMAL, this.makeNoiseBasedOverworld(var0, var1));
            Holder<NoiseGeneratorSettings> var2 = this.noiseSettings.getOrThrow(NoiseGeneratorSettings.LARGE_BIOMES);
            this.registerCustomOverworldPreset(WorldPresets.LARGE_BIOMES, this.makeNoiseBasedOverworld(var0, var2));
            Holder<NoiseGeneratorSettings> var3 = this.noiseSettings.getOrThrow(NoiseGeneratorSettings.AMPLIFIED);
            this.registerCustomOverworldPreset(WorldPresets.AMPLIFIED, this.makeNoiseBasedOverworld(var0, var3));
            Holder.Reference<Biome> var4 = this.biomes.getOrThrow(Biomes.PLAINS);
            this.registerCustomOverworldPreset(WorldPresets.SINGLE_BIOME_SURFACE, this.makeNoiseBasedOverworld(new FixedBiomeSource(var4), var1));
            this.registerCustomOverworldPreset(
                WorldPresets.FLAT,
                this.makeOverworld(new FlatLevelSource(FlatLevelGeneratorSettings.getDefault(this.biomes, this.structureSets, this.placedFeatures)))
            );
            this.registerCustomOverworldPreset(WorldPresets.DEBUG, this.makeOverworld(new DebugLevelSource(var4)));
        }
    }
}

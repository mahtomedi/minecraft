package net.minecraft.client.gui.screens.worldselection;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.client.gui.screens.CreateBuffetWorldScreen;
import net.minecraft.client.gui.screens.CreateFlatWorldScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.biome.OverworldBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class WorldPreset {
    public static final WorldPreset NORMAL = new WorldPreset("default") {
        @Override
        protected ChunkGenerator generator(Registry<Biome> param0, Registry<NoiseGeneratorSettings> param1, long param2) {
            return new NoiseBasedChunkGenerator(
                new OverworldBiomeSource(param2, false, false, param0), param2, () -> param1.getOrThrow(NoiseGeneratorSettings.OVERWORLD)
            );
        }
    };
    private static final WorldPreset FLAT = new WorldPreset("flat") {
        @Override
        protected ChunkGenerator generator(Registry<Biome> param0, Registry<NoiseGeneratorSettings> param1, long param2) {
            return new FlatLevelSource(FlatLevelGeneratorSettings.getDefault());
        }
    };
    private static final WorldPreset LARGE_BIOMES = new WorldPreset("large_biomes") {
        @Override
        protected ChunkGenerator generator(Registry<Biome> param0, Registry<NoiseGeneratorSettings> param1, long param2) {
            return new NoiseBasedChunkGenerator(
                new OverworldBiomeSource(param2, false, true, param0), param2, () -> param1.getOrThrow(NoiseGeneratorSettings.OVERWORLD)
            );
        }
    };
    public static final WorldPreset AMPLIFIED = new WorldPreset("amplified") {
        @Override
        protected ChunkGenerator generator(Registry<Biome> param0, Registry<NoiseGeneratorSettings> param1, long param2) {
            return new NoiseBasedChunkGenerator(
                new OverworldBiomeSource(param2, false, false, param0), param2, () -> param1.getOrThrow(NoiseGeneratorSettings.AMPLIFIED)
            );
        }
    };
    private static final WorldPreset SINGLE_BIOME_SURFACE = new WorldPreset("single_biome_surface") {
        @Override
        protected ChunkGenerator generator(Registry<Biome> param0, Registry<NoiseGeneratorSettings> param1, long param2) {
            return new NoiseBasedChunkGenerator(
                new FixedBiomeSource(param0.getOrThrow(Biomes.PLAINS)), param2, () -> param1.getOrThrow(NoiseGeneratorSettings.OVERWORLD)
            );
        }
    };
    private static final WorldPreset SINGLE_BIOME_CAVES = new WorldPreset("single_biome_caves") {
        @Override
        public WorldGenSettings create(RegistryAccess.RegistryHolder param0, long param1, boolean param2, boolean param3) {
            Registry<Biome> var0 = param0.registryOrThrow(Registry.BIOME_REGISTRY);
            Registry<DimensionType> var1 = param0.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
            Registry<NoiseGeneratorSettings> var2 = param0.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
            return new WorldGenSettings(
                param1,
                param2,
                param3,
                WorldGenSettings.withOverworld(
                    DimensionType.defaultDimensions(var1, var0, var2, param1),
                    () -> var1.getOrThrow(DimensionType.OVERWORLD_CAVES_LOCATION),
                    this.generator(var0, var2, param1)
                )
            );
        }

        @Override
        protected ChunkGenerator generator(Registry<Biome> param0, Registry<NoiseGeneratorSettings> param1, long param2) {
            return new NoiseBasedChunkGenerator(
                new FixedBiomeSource(param0.getOrThrow(Biomes.PLAINS)), param2, () -> param1.getOrThrow(NoiseGeneratorSettings.CAVES)
            );
        }
    };
    private static final WorldPreset SINGLE_BIOME_FLOATING_ISLANDS = new WorldPreset("single_biome_floating_islands") {
        @Override
        protected ChunkGenerator generator(Registry<Biome> param0, Registry<NoiseGeneratorSettings> param1, long param2) {
            return new NoiseBasedChunkGenerator(
                new FixedBiomeSource(param0.getOrThrow(Biomes.PLAINS)), param2, () -> param1.getOrThrow(NoiseGeneratorSettings.FLOATING_ISLANDS)
            );
        }
    };
    private static final WorldPreset DEBUG = new WorldPreset("debug_all_block_states") {
        @Override
        protected ChunkGenerator generator(Registry<Biome> param0, Registry<NoiseGeneratorSettings> param1, long param2) {
            return DebugLevelSource.INSTANCE;
        }
    };
    protected static final List<WorldPreset> PRESETS = Lists.newArrayList(
        NORMAL, FLAT, LARGE_BIOMES, AMPLIFIED, SINGLE_BIOME_SURFACE, SINGLE_BIOME_CAVES, SINGLE_BIOME_FLOATING_ISLANDS, DEBUG
    );
    protected static final Map<Optional<WorldPreset>, WorldPreset.PresetEditor> EDITORS = ImmutableMap.of(
        Optional.of(FLAT),
        (param0, param1) -> {
            ChunkGenerator var0 = param1.overworld();
            return new CreateFlatWorldScreen(
                param0,
                param2 -> param0.worldGenSettingsComponent
                        .updateSettings(
                            new WorldGenSettings(
                                param1.seed(),
                                param1.generateFeatures(),
                                param1.generateBonusChest(),
                                WorldGenSettings.withOverworld(
                                    param0.worldGenSettingsComponent.registryHolder().registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY),
                                    param1.dimensions(),
                                    new FlatLevelSource(param2)
                                )
                            )
                        ),
                var0 instanceof FlatLevelSource ? ((FlatLevelSource)var0).settings() : FlatLevelGeneratorSettings.getDefault()
            );
        },
        Optional.of(SINGLE_BIOME_SURFACE),
        (param0, param1) -> new CreateBuffetWorldScreen(
                param0,
                param0.worldGenSettingsComponent.registryHolder(),
                param2 -> param0.worldGenSettingsComponent
                        .updateSettings(fromBuffetSettings(param0.worldGenSettingsComponent.registryHolder(), param1, SINGLE_BIOME_SURFACE, param2)),
                parseBuffetSettings(param0.worldGenSettingsComponent.registryHolder(), param1)
            ),
        Optional.of(SINGLE_BIOME_CAVES),
        (param0, param1) -> new CreateBuffetWorldScreen(
                param0,
                param0.worldGenSettingsComponent.registryHolder(),
                param2 -> param0.worldGenSettingsComponent
                        .updateSettings(fromBuffetSettings(param0.worldGenSettingsComponent.registryHolder(), param1, SINGLE_BIOME_CAVES, param2)),
                parseBuffetSettings(param0.worldGenSettingsComponent.registryHolder(), param1)
            ),
        Optional.of(SINGLE_BIOME_FLOATING_ISLANDS),
        (param0, param1) -> new CreateBuffetWorldScreen(
                param0,
                param0.worldGenSettingsComponent.registryHolder(),
                param2 -> param0.worldGenSettingsComponent
                        .updateSettings(fromBuffetSettings(param0.worldGenSettingsComponent.registryHolder(), param1, SINGLE_BIOME_FLOATING_ISLANDS, param2)),
                parseBuffetSettings(param0.worldGenSettingsComponent.registryHolder(), param1)
            )
    );
    private final Component description;

    private WorldPreset(String param0) {
        this.description = new TranslatableComponent("generator." + param0);
    }

    private static WorldGenSettings fromBuffetSettings(RegistryAccess param0, WorldGenSettings param1, WorldPreset param2, Biome param3) {
        BiomeSource var0 = new FixedBiomeSource(param3);
        Registry<DimensionType> var1 = param0.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
        Registry<NoiseGeneratorSettings> var2 = param0.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
        Supplier<NoiseGeneratorSettings> var3;
        if (param2 == SINGLE_BIOME_CAVES) {
            var3 = () -> var2.getOrThrow(NoiseGeneratorSettings.CAVES);
        } else if (param2 == SINGLE_BIOME_FLOATING_ISLANDS) {
            var3 = () -> var2.getOrThrow(NoiseGeneratorSettings.FLOATING_ISLANDS);
        } else {
            var3 = () -> var2.getOrThrow(NoiseGeneratorSettings.OVERWORLD);
        }

        return new WorldGenSettings(
            param1.seed(),
            param1.generateFeatures(),
            param1.generateBonusChest(),
            WorldGenSettings.withOverworld(var1, param1.dimensions(), new NoiseBasedChunkGenerator(var0, param1.seed(), var3))
        );
    }

    private static Biome parseBuffetSettings(RegistryAccess param0, WorldGenSettings param1) {
        return param1.overworld()
            .getBiomeSource()
            .possibleBiomes()
            .stream()
            .findFirst()
            .orElse(param0.registryOrThrow(Registry.BIOME_REGISTRY).getOrThrow(Biomes.PLAINS));
    }

    public static Optional<WorldPreset> of(WorldGenSettings param0) {
        ChunkGenerator var0 = param0.overworld();
        if (var0 instanceof FlatLevelSource) {
            return Optional.of(FLAT);
        } else {
            return var0 instanceof DebugLevelSource ? Optional.of(DEBUG) : Optional.empty();
        }
    }

    public Component description() {
        return this.description;
    }

    public WorldGenSettings create(RegistryAccess.RegistryHolder param0, long param1, boolean param2, boolean param3) {
        Registry<Biome> var0 = param0.registryOrThrow(Registry.BIOME_REGISTRY);
        Registry<DimensionType> var1 = param0.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
        Registry<NoiseGeneratorSettings> var2 = param0.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
        return new WorldGenSettings(
            param1,
            param2,
            param3,
            WorldGenSettings.withOverworld(var1, DimensionType.defaultDimensions(var1, var0, var2, param1), this.generator(var0, var2, param1))
        );
    }

    protected abstract ChunkGenerator generator(Registry<Biome> var1, Registry<NoiseGeneratorSettings> var2, long var3);

    @OnlyIn(Dist.CLIENT)
    public interface PresetEditor {
        Screen createEditScreen(CreateWorldScreen var1, WorldGenSettings var2);
    }
}

package net.minecraft.client.gui.screens.worldselection;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.gui.screens.CreateBuffetWorldScreen;
import net.minecraft.client.gui.screens.CreateFlatWorldScreen;
import net.minecraft.client.gui.screens.Screen;
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
        protected ChunkGenerator generator(long param0) {
            return new NoiseBasedChunkGenerator(new OverworldBiomeSource(param0, false, false), param0, NoiseGeneratorSettings.Preset.OVERWORLD.settings());
        }
    };
    private static final WorldPreset FLAT = new WorldPreset("flat") {
        @Override
        protected ChunkGenerator generator(long param0) {
            return new FlatLevelSource(FlatLevelGeneratorSettings.getDefault());
        }
    };
    private static final WorldPreset LARGE_BIOMES = new WorldPreset("large_biomes") {
        @Override
        protected ChunkGenerator generator(long param0) {
            return new NoiseBasedChunkGenerator(new OverworldBiomeSource(param0, false, true), param0, NoiseGeneratorSettings.Preset.OVERWORLD.settings());
        }
    };
    public static final WorldPreset AMPLIFIED = new WorldPreset("amplified") {
        @Override
        protected ChunkGenerator generator(long param0) {
            return new NoiseBasedChunkGenerator(new OverworldBiomeSource(param0, false, false), param0, NoiseGeneratorSettings.Preset.AMPLIFIED.settings());
        }
    };
    private static final WorldPreset SINGLE_BIOME_SURFACE = new WorldPreset("single_biome_surface") {
        @Override
        protected ChunkGenerator generator(long param0) {
            return new NoiseBasedChunkGenerator(new FixedBiomeSource(Biomes.PLAINS), param0, NoiseGeneratorSettings.Preset.OVERWORLD.settings());
        }
    };
    private static final WorldPreset SINGLE_BIOME_CAVES = new WorldPreset("single_biome_caves") {
        @Override
        public WorldGenSettings create(RegistryAccess.RegistryHolder param0, long param1, boolean param2, boolean param3) {
            return new WorldGenSettings(
                param1,
                param2,
                param3,
                WorldGenSettings.withOverworld(DimensionType.defaultDimensions(param1), DimensionType::defaultOverworldCaves, this.generator(param1))
            );
        }

        @Override
        protected ChunkGenerator generator(long param0) {
            return new NoiseBasedChunkGenerator(new FixedBiomeSource(Biomes.PLAINS), param0, NoiseGeneratorSettings.Preset.CAVES.settings());
        }
    };
    private static final WorldPreset SINGLE_BIOME_FLOATING_ISLANDS = new WorldPreset("single_biome_floating_islands") {
        @Override
        protected ChunkGenerator generator(long param0) {
            return new NoiseBasedChunkGenerator(new FixedBiomeSource(Biomes.PLAINS), param0, NoiseGeneratorSettings.Preset.FLOATING_ISLANDS.settings());
        }
    };
    private static final WorldPreset DEBUG = new WorldPreset("debug_all_block_states") {
        @Override
        protected ChunkGenerator generator(long param0) {
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
                                WorldGenSettings.withOverworld(param1.dimensions(), new FlatLevelSource(param2))
                            )
                        ),
                var0 instanceof FlatLevelSource ? ((FlatLevelSource)var0).settings() : FlatLevelGeneratorSettings.getDefault()
            );
        },
        Optional.of(SINGLE_BIOME_SURFACE),
        (param0, param1) -> new CreateBuffetWorldScreen(
                param0,
                param0.worldGenSettingsComponent.registryHolder(),
                param2 -> param0.worldGenSettingsComponent.updateSettings(fromBuffetSettings(param1, SINGLE_BIOME_SURFACE, param2)),
                parseBuffetSettings(param1)
            ),
        Optional.of(SINGLE_BIOME_CAVES),
        (param0, param1) -> new CreateBuffetWorldScreen(
                param0,
                param0.worldGenSettingsComponent.registryHolder(),
                param2 -> param0.worldGenSettingsComponent.updateSettings(fromBuffetSettings(param1, SINGLE_BIOME_CAVES, param2)),
                parseBuffetSettings(param1)
            ),
        Optional.of(SINGLE_BIOME_FLOATING_ISLANDS),
        (param0, param1) -> new CreateBuffetWorldScreen(
                param0,
                param0.worldGenSettingsComponent.registryHolder(),
                param2 -> param0.worldGenSettingsComponent.updateSettings(fromBuffetSettings(param1, SINGLE_BIOME_FLOATING_ISLANDS, param2)),
                parseBuffetSettings(param1)
            )
    );
    private final Component description;

    private WorldPreset(String param0) {
        this.description = new TranslatableComponent("generator." + param0);
    }

    private static WorldGenSettings fromBuffetSettings(WorldGenSettings param0, WorldPreset param1, Biome param2) {
        BiomeSource var0 = new FixedBiomeSource(param2);
        NoiseGeneratorSettings var1;
        if (param1 == SINGLE_BIOME_CAVES) {
            var1 = NoiseGeneratorSettings.Preset.CAVES.settings();
        } else if (param1 == SINGLE_BIOME_FLOATING_ISLANDS) {
            var1 = NoiseGeneratorSettings.Preset.FLOATING_ISLANDS.settings();
        } else {
            var1 = NoiseGeneratorSettings.Preset.OVERWORLD.settings();
        }

        return new WorldGenSettings(
            param0.seed(),
            param0.generateFeatures(),
            param0.generateBonusChest(),
            WorldGenSettings.withOverworld(param0.dimensions(), new NoiseBasedChunkGenerator(var0, param0.seed(), var1))
        );
    }

    private static Biome parseBuffetSettings(WorldGenSettings param0) {
        return param0.overworld().getBiomeSource().possibleBiomes().stream().findFirst().orElse(Biomes.PLAINS);
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
        return new WorldGenSettings(param1, param2, param3, WorldGenSettings.withOverworld(DimensionType.defaultDimensions(param1), this.generator(param1)));
    }

    protected abstract ChunkGenerator generator(long var1);

    @OnlyIn(Dist.CLIENT)
    public interface PresetEditor {
        Screen createEditScreen(CreateWorldScreen var1, WorldGenSettings var2);
    }
}

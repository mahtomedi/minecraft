package net.minecraft.client.gui.screens.worldselection;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.gui.screens.CreateBuffetWorldScreen;
import net.minecraft.client.gui.screens.CreateFlatWorldScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
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
        protected ChunkGenerator generator(RegistryAccess param0, long param1) {
            return WorldGenSettings.makeDefaultOverworld(param0, param1);
        }
    };
    private static final WorldPreset FLAT = new WorldPreset("flat") {
        @Override
        protected ChunkGenerator generator(RegistryAccess param0, long param1) {
            Registry<Biome> var0 = param0.registryOrThrow(Registry.BIOME_REGISTRY);
            return new FlatLevelSource(FlatLevelGeneratorSettings.getDefault(var0));
        }
    };
    public static final WorldPreset LARGE_BIOMES = new WorldPreset("large_biomes") {
        @Override
        protected ChunkGenerator generator(RegistryAccess param0, long param1) {
            return WorldGenSettings.makeOverworld(param0, param1, NoiseGeneratorSettings.LARGE_BIOMES);
        }
    };
    public static final WorldPreset AMPLIFIED = new WorldPreset("amplified") {
        @Override
        protected ChunkGenerator generator(RegistryAccess param0, long param1) {
            return WorldGenSettings.makeOverworld(param0, param1, NoiseGeneratorSettings.AMPLIFIED);
        }
    };
    private static final WorldPreset SINGLE_BIOME_SURFACE = new WorldPreset("single_biome_surface") {
        @Override
        protected ChunkGenerator generator(RegistryAccess param0, long param1) {
            return WorldPreset.fixedBiomeGenerator(param0, param1, NoiseGeneratorSettings.OVERWORLD);
        }
    };
    private static final WorldPreset DEBUG = new WorldPreset("debug_all_block_states") {
        @Override
        protected ChunkGenerator generator(RegistryAccess param0, long param1) {
            return new DebugLevelSource(param0.registryOrThrow(Registry.BIOME_REGISTRY));
        }
    };
    protected static final List<WorldPreset> PRESETS = Lists.newArrayList(NORMAL, FLAT, LARGE_BIOMES, AMPLIFIED, SINGLE_BIOME_SURFACE, DEBUG);
    protected static final Map<Optional<WorldPreset>, WorldPreset.PresetEditor> EDITORS = ImmutableMap.of(
        Optional.of(FLAT),
        (param0, param1) -> {
            ChunkGenerator var0 = param1.overworld();
            Registry<Biome> var1 = param0.worldGenSettingsComponent.registryHolder().registryOrThrow(Registry.BIOME_REGISTRY);
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
                var0 instanceof FlatLevelSource ? ((FlatLevelSource)var0).settings() : FlatLevelGeneratorSettings.getDefault(var1)
            );
        },
        Optional.of(SINGLE_BIOME_SURFACE),
        (param0, param1) -> new CreateBuffetWorldScreen(
                param0,
                param0.worldGenSettingsComponent.registryHolder(),
                param2 -> param0.worldGenSettingsComponent
                        .updateSettings(fromBuffetSettings(param0.worldGenSettingsComponent.registryHolder(), param1, param2)),
                parseBuffetSettings(param0.worldGenSettingsComponent.registryHolder(), param1)
            )
    );
    private final Component description;

    static NoiseBasedChunkGenerator fixedBiomeGenerator(RegistryAccess param0, long param1, ResourceKey<NoiseGeneratorSettings> param2) {
        return new NoiseBasedChunkGenerator(
            param0.registryOrThrow(Registry.NOISE_REGISTRY),
            new FixedBiomeSource(param0.registryOrThrow(Registry.BIOME_REGISTRY).getOrCreateHolder(Biomes.PLAINS)),
            param1,
            param0.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY).getOrCreateHolder(param2)
        );
    }

    WorldPreset(String param0) {
        this.description = new TranslatableComponent("generator." + param0);
    }

    private static WorldGenSettings fromBuffetSettings(RegistryAccess param0, WorldGenSettings param1, Holder<Biome> param2) {
        BiomeSource var0 = new FixedBiomeSource(param2);
        Registry<DimensionType> var1 = param0.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
        Registry<NoiseGeneratorSettings> var2 = param0.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
        Holder<NoiseGeneratorSettings> var3 = var2.getOrCreateHolder(NoiseGeneratorSettings.OVERWORLD);
        return new WorldGenSettings(
            param1.seed(),
            param1.generateFeatures(),
            param1.generateBonusChest(),
            WorldGenSettings.withOverworld(
                var1, param1.dimensions(), new NoiseBasedChunkGenerator(param0.registryOrThrow(Registry.NOISE_REGISTRY), var0, param1.seed(), var3)
            )
        );
    }

    private static Holder<Biome> parseBuffetSettings(RegistryAccess param0, WorldGenSettings param1) {
        return param1.overworld()
            .getBiomeSource()
            .possibleBiomes()
            .findFirst()
            .orElse(param0.registryOrThrow(Registry.BIOME_REGISTRY).getOrCreateHolder(Biomes.PLAINS));
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

    public WorldGenSettings create(RegistryAccess param0, long param1, boolean param2, boolean param3) {
        return new WorldGenSettings(
            param1,
            param2,
            param3,
            WorldGenSettings.withOverworld(
                param0.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY), DimensionType.defaultDimensions(param0, param1), this.generator(param0, param1)
            )
        );
    }

    protected abstract ChunkGenerator generator(RegistryAccess var1, long var2);

    public static boolean isVisibleByDefault(WorldPreset param0) {
        return param0 != DEBUG;
    }

    @OnlyIn(Dist.CLIENT)
    public interface PresetEditor {
        Screen createEditScreen(CreateWorldScreen var1, WorldGenSettings var2);
    }
}

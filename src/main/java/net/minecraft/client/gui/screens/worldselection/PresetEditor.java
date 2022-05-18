package net.minecraft.client.gui.screens.worldselection;

import java.util.Map;
import java.util.Optional;
import net.minecraft.client.gui.screens.CreateBuffetWorldScreen;
import net.minecraft.client.gui.screens.CreateFlatWorldScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface PresetEditor {
    Map<Optional<ResourceKey<WorldPreset>>, PresetEditor> EDITORS = Map.of(
        Optional.of(WorldPresets.FLAT),
        (param0, param1) -> {
            ChunkGenerator var0 = param1.worldGenSettings().overworld();
            RegistryAccess var1 = param1.registryAccess();
            Registry<Biome> var2 = var1.registryOrThrow(Registry.BIOME_REGISTRY);
            Registry<StructureSet> var3 = var1.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
            return new CreateFlatWorldScreen(
                param0,
                param1x -> param0.worldGenSettingsComponent.updateSettings(flatWorldConfigurator(param1x)),
                var0 instanceof FlatLevelSource ? ((FlatLevelSource)var0).settings() : FlatLevelGeneratorSettings.getDefault(var2, var3)
            );
        },
        Optional.of(WorldPresets.SINGLE_BIOME_SURFACE),
        (param0, param1) -> new CreateBuffetWorldScreen(
                param0, param1, param1x -> param0.worldGenSettingsComponent.updateSettings(fixedBiomeConfigurator(param1x))
            )
    );

    Screen createEditScreen(CreateWorldScreen var1, WorldCreationContext var2);

    private static WorldCreationContext.Updater flatWorldConfigurator(FlatLevelGeneratorSettings param0) {
        return (param1, param2) -> {
            Registry<StructureSet> var0x = param1.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
            ChunkGenerator var1 = new FlatLevelSource(var0x, param0);
            return WorldGenSettings.replaceOverworldGenerator(param1, param2, var1);
        };
    }

    private static WorldCreationContext.Updater fixedBiomeConfigurator(Holder<Biome> param0) {
        return (param1, param2) -> {
            Registry<StructureSet> var0x = param1.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
            Registry<NoiseGeneratorSettings> var1 = param1.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
            Registry<NormalNoise.NoiseParameters> var2 = param1.registryOrThrow(Registry.NOISE_REGISTRY);
            Holder<NoiseGeneratorSettings> var3 = var1.getOrCreateHolderOrThrow(NoiseGeneratorSettings.OVERWORLD);
            BiomeSource var4 = new FixedBiomeSource(param0);
            ChunkGenerator var5 = new NoiseBasedChunkGenerator(var0x, var2, var4, var3);
            return WorldGenSettings.replaceOverworldGenerator(param1, param2, var5);
        };
    }
}

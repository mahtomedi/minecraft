package net.minecraft.client.gui.screens.worldselection;

import java.util.Map;
import java.util.Optional;
import net.minecraft.client.gui.screens.CreateBuffetWorldScreen;
import net.minecraft.client.gui.screens.CreateFlatWorldScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface PresetEditor {
    Map<Optional<ResourceKey<WorldPreset>>, PresetEditor> EDITORS = Map.of(
        Optional.of(WorldPresets.FLAT),
        (PresetEditor)(param0, param1) -> {
            ChunkGenerator var0 = param1.selectedDimensions().overworld();
            RegistryAccess var1 = param1.worldgenLoadContext();
            HolderGetter<Biome> var2 = var1.lookupOrThrow(Registries.BIOME);
            HolderGetter<StructureSet> var3 = var1.lookupOrThrow(Registries.STRUCTURE_SET);
            HolderGetter<PlacedFeature> var4 = var1.lookupOrThrow(Registries.PLACED_FEATURE);
            return new CreateFlatWorldScreen(
                param0,
                param1x -> param0.getUiState().updateDimensions(flatWorldConfigurator(param1x)),
                var0 instanceof FlatLevelSource ? ((FlatLevelSource)var0).settings() : FlatLevelGeneratorSettings.getDefault(var2, var3, var4)
            );
        },
        Optional.of(WorldPresets.SINGLE_BIOME_SURFACE),
        (PresetEditor)(param0, param1) -> new CreateBuffetWorldScreen(
                param0, param1, param1x -> param0.getUiState().updateDimensions(fixedBiomeConfigurator(param1x))
            )
    );

    Screen createEditScreen(CreateWorldScreen var1, WorldCreationContext var2);

    private static WorldCreationContext.DimensionsUpdater flatWorldConfigurator(FlatLevelGeneratorSettings param0) {
        return (param1, param2) -> {
            ChunkGenerator var0x = new FlatLevelSource(param0);
            return param2.replaceOverworldGenerator(param1, var0x);
        };
    }

    private static WorldCreationContext.DimensionsUpdater fixedBiomeConfigurator(Holder<Biome> param0) {
        return (param1, param2) -> {
            Registry<NoiseGeneratorSettings> var0x = param1.registryOrThrow(Registries.NOISE_SETTINGS);
            Holder<NoiseGeneratorSettings> var1 = var0x.getHolderOrThrow(NoiseGeneratorSettings.OVERWORLD);
            BiomeSource var2 = new FixedBiomeSource(param0);
            ChunkGenerator var3 = new NoiseBasedChunkGenerator(var2, var1);
            return param2.replaceOverworldGenerator(param1, var3);
        };
    }
}

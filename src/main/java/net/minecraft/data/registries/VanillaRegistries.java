package net.minecraft.data.registries;

import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.worldgen.Carvers;
import net.minecraft.data.worldgen.DimensionTypes;
import net.minecraft.data.worldgen.NoiseData;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.data.worldgen.StructureSets;
import net.minecraft.data.worldgen.Structures;
import net.minecraft.data.worldgen.biome.Biomes;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.network.chat.ChatType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouterData;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPresets;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

public class VanillaRegistries {
    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
        .add(Registry.DIMENSION_TYPE_REGISTRY, DimensionTypes::bootstrap)
        .add(Registry.CONFIGURED_CARVER_REGISTRY, Carvers::bootstrap)
        .add(Registry.CONFIGURED_FEATURE_REGISTRY, FeatureUtils::bootstrap)
        .add(Registry.PLACED_FEATURE_REGISTRY, PlacementUtils::bootstrap)
        .add(Registry.STRUCTURE_REGISTRY, Structures::bootstrap)
        .add(Registry.STRUCTURE_SET_REGISTRY, StructureSets::bootstrap)
        .add(Registry.PROCESSOR_LIST_REGISTRY, ProcessorLists::bootstrap)
        .add(Registry.TEMPLATE_POOL_REGISTRY, Pools::bootstrap)
        .add(Registry.BIOME_REGISTRY, Biomes::bootstrap)
        .add(Registry.NOISE_REGISTRY, NoiseData::bootstrap)
        .add(Registry.DENSITY_FUNCTION_REGISTRY, NoiseRouterData::bootstrap)
        .add(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, NoiseGeneratorSettings::bootstrap)
        .add(Registry.WORLD_PRESET_REGISTRY, WorldPresets::bootstrap)
        .add(Registry.FLAT_LEVEL_GENERATOR_PRESET_REGISTRY, FlatLevelGeneratorPresets::bootstrap)
        .add(Registry.CHAT_TYPE_REGISTRY, ChatType::bootstrap);

    private static void validateThatAllBiomeFeaturesHaveBiomeFilter(HolderLookup.Provider param0) {
        HolderGetter<PlacedFeature> var0 = param0.lookupOrThrow(Registry.PLACED_FEATURE_REGISTRY);
        param0.lookupOrThrow(Registry.BIOME_REGISTRY).listElements().forEach(param1 -> {
            ResourceLocation var0x = param1.key().location();
            List<HolderSet<PlacedFeature>> var1x = ((Biome)param1.value()).getGenerationSettings().features();
            var1x.stream().flatMap(HolderSet::stream).forEach(param3 -> param3.unwrap().ifLeft(param2x -> {
                    Holder.Reference<PlacedFeature> var0xx = var0.getOrThrow(param2x);
                    if (!validatePlacedFeature((PlacedFeature)var0xx.value())) {
                        Util.logAndPauseIfInIde("Placed feature " + param2x.location() + " in biome " + var0x + " is missing BiomeFilter.biome()");
                    }

                }).ifRight(param1x -> {
                    if (!validatePlacedFeature(param1x)) {
                        Util.logAndPauseIfInIde("Placed inline feature in biome " + param1 + " is missing BiomeFilter.biome()");
                    }

                }));
        });
    }

    private static boolean validatePlacedFeature(PlacedFeature param0) {
        return param0.placement().contains(BiomeFilter.biome());
    }

    public static HolderLookup.Provider createLookup() {
        RegistryAccess.Frozen var0 = RegistryAccess.fromRegistryOfRegistries(Registry.REGISTRY);
        HolderLookup.Provider var1 = BUILDER.build(var0);
        validateThatAllBiomeFeaturesHaveBiomeFilter(var1);
        return var1;
    }
}

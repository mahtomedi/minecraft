package net.minecraft.data.registries;

import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.Carvers;
import net.minecraft.data.worldgen.DimensionTypes;
import net.minecraft.data.worldgen.NoiseData;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.data.worldgen.StructureSets;
import net.minecraft.data.worldgen.Structures;
import net.minecraft.data.worldgen.biome.BiomeData;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.network.chat.ChatType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.item.armortrim.TrimPatterns;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouterData;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPresets;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

public class VanillaRegistries {
    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
        .add(Registries.DIMENSION_TYPE, DimensionTypes::bootstrap)
        .add(Registries.CONFIGURED_CARVER, Carvers::bootstrap)
        .add(Registries.CONFIGURED_FEATURE, FeatureUtils::bootstrap)
        .add(Registries.PLACED_FEATURE, PlacementUtils::bootstrap)
        .add(Registries.STRUCTURE, Structures::bootstrap)
        .add(Registries.STRUCTURE_SET, StructureSets::bootstrap)
        .add(Registries.PROCESSOR_LIST, ProcessorLists::bootstrap)
        .add(Registries.TEMPLATE_POOL, Pools::bootstrap)
        .add(Registries.BIOME, BiomeData::bootstrap)
        .add(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST, MultiNoiseBiomeSourceParameterLists::bootstrap)
        .add(Registries.NOISE, NoiseData::bootstrap)
        .add(Registries.DENSITY_FUNCTION, NoiseRouterData::bootstrap)
        .add(Registries.NOISE_SETTINGS, NoiseGeneratorSettings::bootstrap)
        .add(Registries.WORLD_PRESET, WorldPresets::bootstrap)
        .add(Registries.FLAT_LEVEL_GENERATOR_PRESET, FlatLevelGeneratorPresets::bootstrap)
        .add(Registries.CHAT_TYPE, ChatType::bootstrap)
        .add(Registries.TRIM_PATTERN, TrimPatterns::bootstrap)
        .add(Registries.TRIM_MATERIAL, TrimMaterials::bootstrap)
        .add(Registries.DAMAGE_TYPE, DamageTypes::bootstrap);

    private static void validateThatAllBiomeFeaturesHaveBiomeFilter(HolderLookup.Provider param0) {
        validateThatAllBiomeFeaturesHaveBiomeFilter(param0.lookupOrThrow(Registries.PLACED_FEATURE), param0.lookupOrThrow(Registries.BIOME));
    }

    public static void validateThatAllBiomeFeaturesHaveBiomeFilter(HolderGetter<PlacedFeature> param0, HolderLookup<Biome> param1) {
        param1.listElements().forEach(param1x -> {
            ResourceLocation var0x = param1x.key().location();
            List<HolderSet<PlacedFeature>> var1x = param1x.value().getGenerationSettings().features();
            var1x.stream().flatMap(HolderSet::stream).forEach(param3 -> param3.unwrap().ifLeft(param2x -> {
                    Holder.Reference<PlacedFeature> var0xx = param0.getOrThrow(param2x);
                    if (!validatePlacedFeature((PlacedFeature)var0xx.value())) {
                        Util.logAndPauseIfInIde("Placed feature " + param2x.location() + " in biome " + var0x + " is missing BiomeFilter.biome()");
                    }

                }).ifRight(param1xxx -> {
                    if (!validatePlacedFeature(param1xxx)) {
                        Util.logAndPauseIfInIde("Placed inline feature in biome " + param1x + " is missing BiomeFilter.biome()");
                    }

                }));
        });
    }

    private static boolean validatePlacedFeature(PlacedFeature param0) {
        return param0.placement().contains(BiomeFilter.biome());
    }

    public static HolderLookup.Provider createLookup() {
        RegistryAccess.Frozen var0 = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        HolderLookup.Provider var1 = BUILDER.build(var0);
        validateThatAllBiomeFeaturesHaveBiomeFilter(var1);
        return var1;
    }
}

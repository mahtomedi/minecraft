package net.minecraft.data.registries;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.biome.BiomeData;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.item.armortrim.TrimPatterns;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

public class UpdateOneTwentyRegistries {
    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
        .add(Registries.TRIM_MATERIAL, TrimMaterials::nextUpdate)
        .add(Registries.TRIM_PATTERN, TrimPatterns::nextUpdate)
        .add(Registries.BIOME, BiomeData::nextUpdate)
        .add(Registries.WORLD_PRESET, WorldPresets::nextUpdate);

    public static CompletableFuture<HolderLookup.Provider> createLookup(CompletableFuture<HolderLookup.Provider> param0) {
        return param0.thenApply(
            param0x -> {
                RegistryAccess.Frozen var0x = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
                HolderLookup.Provider var1 = BUILDER.buildPatch(var0x, param0x);
                VanillaRegistries.validateThatAllBiomeFeaturesHaveBiomeFilter(
                    param0x.lookupOrThrow(Registries.PLACED_FEATURE), var1.lookupOrThrow(Registries.BIOME)
                );
                return var1;
            }
        );
    }
}

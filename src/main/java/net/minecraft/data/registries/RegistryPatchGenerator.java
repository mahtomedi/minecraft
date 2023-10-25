package net.minecraft.data.registries;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class RegistryPatchGenerator {
    public static CompletableFuture<HolderLookup.Provider> createLookup(CompletableFuture<HolderLookup.Provider> param0, RegistrySetBuilder param1) {
        return param0.thenApply(
            param1x -> {
                RegistryAccess.Frozen var0x = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
                HolderLookup.Provider var1x = param1.buildPatch(var0x, param1x);
                Optional<HolderLookup.RegistryLookup<Biome>> var2 = var1x.lookup(Registries.BIOME);
                Optional<HolderLookup.RegistryLookup<PlacedFeature>> var3 = var1x.lookup(Registries.PLACED_FEATURE);
                if (var2.isPresent() || var3.isPresent()) {
                    VanillaRegistries.validateThatAllBiomeFeaturesHaveBiomeFilter(
                        var3.orElseGet(() -> param1x.lookupOrThrow(Registries.PLACED_FEATURE)), var2.orElseGet(() -> param1x.lookupOrThrow(Registries.BIOME))
                    );
                }
    
                return var1x;
            }
        );
    }
}

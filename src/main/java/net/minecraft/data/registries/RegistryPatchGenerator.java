package net.minecraft.data.registries;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.Cloner;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class RegistryPatchGenerator {
    public static CompletableFuture<RegistrySetBuilder.PatchedRegistries> createLookup(
        CompletableFuture<HolderLookup.Provider> param0, RegistrySetBuilder param1
    ) {
        return param0.thenApply(
            param1x -> {
                RegistryAccess.Frozen var0x = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
                Cloner.Factory var1x = new Cloner.Factory();
                RegistryDataLoader.WORLDGEN_REGISTRIES.forEach(param1xx -> param1xx.runWithArguments(var1x::addCodec));
                RegistrySetBuilder.PatchedRegistries var2 = param1.buildPatch(var0x, param1x, var1x);
                HolderLookup.Provider var3 = var2.full();
                Optional<HolderLookup.RegistryLookup<Biome>> var4 = var3.lookup(Registries.BIOME);
                Optional<HolderLookup.RegistryLookup<PlacedFeature>> var5 = var3.lookup(Registries.PLACED_FEATURE);
                if (var4.isPresent() || var5.isPresent()) {
                    VanillaRegistries.validateThatAllBiomeFeaturesHaveBiomeFilter(
                        var5.orElseGet(() -> param1x.lookupOrThrow(Registries.PLACED_FEATURE)), var4.orElseGet(() -> param1x.lookupOrThrow(Registries.BIOME))
                    );
                }
    
                return var2;
            }
        );
    }
}

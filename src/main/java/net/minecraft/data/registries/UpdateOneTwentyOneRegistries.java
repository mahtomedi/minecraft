package net.minecraft.data.registries;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;

public class UpdateOneTwentyOneRegistries {
    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder();

    public static CompletableFuture<HolderLookup.Provider> createLookup(CompletableFuture<HolderLookup.Provider> param0) {
        return RegistryPatchGenerator.createLookup(param0, BUILDER);
    }
}

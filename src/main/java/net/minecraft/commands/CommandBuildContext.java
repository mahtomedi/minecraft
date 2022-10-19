package net.minecraft.commands;

import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlagSet;

public final class CommandBuildContext {
    private final RegistryAccess registryAccess;
    private final FeatureFlagSet enabledFeatures;
    CommandBuildContext.MissingTagAccessPolicy missingTagAccessPolicy = CommandBuildContext.MissingTagAccessPolicy.FAIL;

    public CommandBuildContext(RegistryAccess param0, FeatureFlagSet param1) {
        this.registryAccess = param0;
        this.enabledFeatures = param1;
    }

    public void missingTagAccessPolicy(CommandBuildContext.MissingTagAccessPolicy param0) {
        this.missingTagAccessPolicy = param0;
    }

    public <T> HolderLookup<T> holderLookup(ResourceKey<? extends Registry<T>> param0) {
        HolderLookup.RegistryLookup<T> var0 = new HolderLookup.RegistryLookup<T>(this.registryAccess.registryOrThrow(param0)) {
            @Override
            public Optional<HolderSet.Named<T>> get(TagKey<T> param0) {
                return switch(CommandBuildContext.this.missingTagAccessPolicy) {
                    case FAIL -> this.registry.getTag(param0);
                    case CREATE_NEW -> Optional.of(this.registry.getOrCreateTag(param0));
                    case RETURN_EMPTY -> {
                        Optional<HolderSet.Named<T>> var0 = this.registry.getTag(param0);
                        yield Optional.of(var0.orElseGet(() -> HolderSet.emptyNamed(this.registry, param0)));
                    }
                };
            }
        };
        return var0.filterFeatures(this.enabledFeatures);
    }

    public static enum MissingTagAccessPolicy {
        CREATE_NEW,
        RETURN_EMPTY,
        FAIL;
    }
}

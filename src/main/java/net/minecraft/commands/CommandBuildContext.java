package net.minecraft.commands;

import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

public final class CommandBuildContext {
    private final RegistryAccess registryAccess;
    CommandBuildContext.MissingTagAccessPolicy missingTagAccessPolicy = CommandBuildContext.MissingTagAccessPolicy.FAIL;

    public CommandBuildContext(RegistryAccess param0) {
        this.registryAccess = param0;
    }

    public void missingTagAccessPolicy(CommandBuildContext.MissingTagAccessPolicy param0) {
        this.missingTagAccessPolicy = param0;
    }

    public <T> HolderLookup<T> holderLookup(ResourceKey<? extends Registry<T>> param0) {
        return new HolderLookup.RegistryLookup<T>(this.registryAccess.registryOrThrow(param0)) {
            @Override
            public Optional<? extends HolderSet<T>> get(TagKey<T> param0) {
                return switch(CommandBuildContext.this.missingTagAccessPolicy) {
                    case FAIL -> this.registry.getTag(param0);
                    case CREATE_NEW -> Optional.of(this.registry.getOrCreateTag(param0));
                    case RETURN_EMPTY -> {
                        Optional<? extends HolderSet<T>> var0 = this.registry.getTag(param0);
                        yield Optional.of(var0.isPresent() ? var0.get() : HolderSet.direct());
                    }
                };
            }
        };
    }

    public static enum MissingTagAccessPolicy {
        CREATE_NEW,
        RETURN_EMPTY,
        FAIL;
    }
}

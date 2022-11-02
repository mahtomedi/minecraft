package net.minecraft.commands;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;

public interface CommandBuildContext {
    <T> HolderLookup<T> holderLookup(ResourceKey<? extends Registry<T>> var1);

    static CommandBuildContext simple(final HolderLookup.Provider param0, final FeatureFlagSet param1) {
        return new CommandBuildContext() {
            @Override
            public <T> HolderLookup<T> holderLookup(ResourceKey<? extends Registry<T>> param0x) {
                return param0.<T>lookupOrThrow(param0).filterFeatures(param1);
            }
        };
    }

    static CommandBuildContext.Configurable configurable(final RegistryAccess param0, final FeatureFlagSet param1) {
        return new CommandBuildContext.Configurable() {
            CommandBuildContext.MissingTagAccessPolicy missingTagAccessPolicy = CommandBuildContext.MissingTagAccessPolicy.FAIL;

            @Override
            public void missingTagAccessPolicy(CommandBuildContext.MissingTagAccessPolicy param0x) {
                this.missingTagAccessPolicy = param0;
            }

            @Override
            public <T> HolderLookup<T> holderLookup(ResourceKey<? extends Registry<T>> param0x) {
                Registry<T> var0 = param0.registryOrThrow(param0);
                final HolderLookup.RegistryLookup<T> var1 = var0.asLookup();
                final HolderLookup.RegistryLookup<T> var2 = var0.asTagAddingLookup();
                HolderLookup.RegistryLookup<T> var3 = new HolderLookup.RegistryLookup.Delegate<T>() {
                    @Override
                    protected HolderLookup.RegistryLookup<T> parent() {
                        return switch(missingTagAccessPolicy) {
                            case FAIL -> var1;
                            case CREATE_NEW -> var2;
                        };
                    }
                };
                return var3.filterFeatures(param1);
            }
        };
    }

    public interface Configurable extends CommandBuildContext {
        void missingTagAccessPolicy(CommandBuildContext.MissingTagAccessPolicy var1);
    }

    public static enum MissingTagAccessPolicy {
        CREATE_NEW,
        FAIL;
    }
}

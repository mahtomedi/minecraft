package net.minecraft.resources;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.util.ExtraCodecs;

public class RegistryOps<T> extends DelegatingOps<T> {
    private final RegistryOps.RegistryInfoLookup lookupProvider;

    private static RegistryOps.RegistryInfoLookup memoizeLookup(final RegistryOps.RegistryInfoLookup param0) {
        return new RegistryOps.RegistryInfoLookup() {
            private final Map<ResourceKey<? extends Registry<?>>, Optional<? extends RegistryOps.RegistryInfo<?>>> lookups = new HashMap<>();

            @Override
            public <T> Optional<RegistryOps.RegistryInfo<T>> lookup(ResourceKey<? extends Registry<? extends T>> param0x) {
                return this.lookups.computeIfAbsent(param0, param0::lookup);
            }
        };
    }

    public static <T> RegistryOps<T> create(DynamicOps<T> param0, final HolderLookup.Provider param1) {
        return create(param0, memoizeLookup(new RegistryOps.RegistryInfoLookup() {
            @Override
            public <E> Optional<RegistryOps.RegistryInfo<E>> lookup(ResourceKey<? extends Registry<? extends E>> param0) {
                return param1.lookup(param0).map(param0x -> new RegistryOps.RegistryInfo<>(param0x, param0x, param0x.registryLifecycle()));
            }
        }));
    }

    public static <T> RegistryOps<T> create(DynamicOps<T> param0, RegistryOps.RegistryInfoLookup param1) {
        return new RegistryOps<>(param0, param1);
    }

    private RegistryOps(DynamicOps<T> param0, RegistryOps.RegistryInfoLookup param1) {
        super(param0);
        this.lookupProvider = param1;
    }

    public <E> Optional<HolderOwner<E>> owner(ResourceKey<? extends Registry<? extends E>> param0) {
        return this.lookupProvider.lookup(param0).map(RegistryOps.RegistryInfo::owner);
    }

    public <E> Optional<HolderGetter<E>> getter(ResourceKey<? extends Registry<? extends E>> param0) {
        return this.lookupProvider.lookup(param0).map(RegistryOps.RegistryInfo::getter);
    }

    public static <E, O> RecordCodecBuilder<O, HolderGetter<E>> retrieveGetter(ResourceKey<? extends Registry<? extends E>> param0) {
        return ExtraCodecs.retrieveContext(
                param1 -> param1 instanceof RegistryOps var0x
                        ? var0x.lookupProvider
                            .lookup(param0)
                            .map(param0x -> DataResult.success(param0x.getter(), param0x.elementsLifecycle()))
                            .orElseGet(() -> DataResult.error("Unknown registry: " + param0))
                        : DataResult.error("Not a registry ops")
            )
            .forGetter(param0x -> null);
    }

    public static <E, O> RecordCodecBuilder<O, Holder.Reference<E>> retrieveElement(ResourceKey<E> param0) {
        ResourceKey<? extends Registry<E>> var0 = ResourceKey.createRegistryKey(param0.registry());
        return ExtraCodecs.retrieveContext(
                param2 -> param2 instanceof RegistryOps var0x
                        ? var0x.lookupProvider
                            .lookup(var0)
                            .flatMap(param1x -> param1x.getter().get(param0))
                            .map(DataResult::success)
                            .orElseGet(() -> DataResult.error("Can't find value: " + param0))
                        : DataResult.error("Not a registry ops")
            )
            .forGetter(param0x -> null);
    }

    public static record RegistryInfo<T>(HolderOwner<T> owner, HolderGetter<T> getter, Lifecycle elementsLifecycle) {
    }

    public interface RegistryInfoLookup {
        <T> Optional<RegistryOps.RegistryInfo<T>> lookup(ResourceKey<? extends Registry<? extends T>> var1);
    }
}

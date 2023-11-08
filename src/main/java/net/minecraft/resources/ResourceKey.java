package net.minecraft.resources;

import com.google.common.collect.MapMaker;
import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;

public class ResourceKey<T> {
    private static final ConcurrentMap<ResourceKey.InternKey, ResourceKey<?>> VALUES = new MapMaker().weakValues().makeMap();
    private final ResourceLocation registryName;
    private final ResourceLocation location;

    public static <T> Codec<ResourceKey<T>> codec(ResourceKey<? extends Registry<T>> param0) {
        return ResourceLocation.CODEC.xmap(param1 -> create(param0, param1), ResourceKey::location);
    }

    public static <T> ResourceKey<T> create(ResourceKey<? extends Registry<T>> param0, ResourceLocation param1) {
        return create(param0.location, param1);
    }

    public static <T> ResourceKey<Registry<T>> createRegistryKey(ResourceLocation param0) {
        return create(Registries.ROOT_REGISTRY_NAME, param0);
    }

    private static <T> ResourceKey<T> create(ResourceLocation param0, ResourceLocation param1) {
        return (ResourceKey<T>)VALUES.computeIfAbsent(new ResourceKey.InternKey(param0, param1), param0x -> new ResourceKey(param0x.registry, param0x.location));
    }

    private ResourceKey(ResourceLocation param0, ResourceLocation param1) {
        this.registryName = param0;
        this.location = param1;
    }

    @Override
    public String toString() {
        return "ResourceKey[" + this.registryName + " / " + this.location + "]";
    }

    public boolean isFor(ResourceKey<? extends Registry<?>> param0) {
        return this.registryName.equals(param0.location());
    }

    public <E> Optional<ResourceKey<E>> cast(ResourceKey<? extends Registry<E>> param0) {
        return this.isFor(param0) ? Optional.of(this) : Optional.empty();
    }

    public ResourceLocation location() {
        return this.location;
    }

    public ResourceLocation registry() {
        return this.registryName;
    }

    static record InternKey(ResourceLocation registry, ResourceLocation location) {
    }
}

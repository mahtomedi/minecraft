package net.minecraft.resources;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.Registry;

public class ResourceKey<T> {
    private static final Map<String, ResourceKey<?>> VALUES = Collections.synchronizedMap(Maps.newIdentityHashMap());
    private final ResourceLocation registryName;
    private final ResourceLocation location;

    public static <T> Codec<ResourceKey<T>> codec(ResourceKey<? extends Registry<T>> param0) {
        return ResourceLocation.CODEC.xmap(param1 -> create(param0, param1), ResourceKey::location);
    }

    public static <T> ResourceKey<T> create(ResourceKey<? extends Registry<T>> param0, ResourceLocation param1) {
        return create(param0.location, param1);
    }

    public static <T> ResourceKey<Registry<T>> createRegistryKey(ResourceLocation param0) {
        return create(Registry.ROOT_REGISTRY_NAME, param0);
    }

    private static <T> ResourceKey<T> create(ResourceLocation param0, ResourceLocation param1) {
        String var0 = (param0 + ":" + param1).intern();
        return (ResourceKey<T>)VALUES.computeIfAbsent(var0, param2 -> new ResourceKey(param0, param1));
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

    public static <T> Function<ResourceLocation, ResourceKey<T>> elementKey(ResourceKey<? extends Registry<T>> param0) {
        return param1 -> create(param0, param1);
    }
}

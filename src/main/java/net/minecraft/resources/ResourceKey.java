package net.minecraft.resources;

import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.core.Registry;

public class ResourceKey<T> {
    private static final Map<String, ResourceKey<?>> VALUES = Collections.synchronizedMap(Maps.newIdentityHashMap());
    private final ResourceLocation registryName;
    private final ResourceLocation location;

    public static <T> ResourceKey<T> create(ResourceKey<Registry<T>> param0, ResourceLocation param1) {
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
        return "ResourceKey[" + this.registryName + " / " + this.location + ']';
    }

    public ResourceLocation location() {
        return this.location;
    }

    public static <T> Function<ResourceLocation, ResourceKey<T>> elementKey(ResourceKey<Registry<T>> param0) {
        return param1 -> create(param0, param1);
    }
}
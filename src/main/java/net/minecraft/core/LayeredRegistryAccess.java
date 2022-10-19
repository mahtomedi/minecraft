package net.minecraft.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.resources.ResourceKey;

public class LayeredRegistryAccess<T> {
    private final List<T> keys;
    private final List<RegistryAccess.Frozen> values;
    private final RegistryAccess.Frozen composite;

    public LayeredRegistryAccess(List<T> param0) {
        this(param0, Util.make(() -> {
            RegistryAccess.Frozen[] var0 = new RegistryAccess.Frozen[param0.size()];
            Arrays.fill(var0, RegistryAccess.EMPTY);
            return Arrays.asList(var0);
        }));
    }

    private LayeredRegistryAccess(List<T> param0, List<RegistryAccess.Frozen> param1) {
        this.keys = List.copyOf(param0);
        this.values = List.copyOf(param1);
        this.composite = new RegistryAccess.ImmutableRegistryAccess(collectRegistries(param1.stream())).freeze();
    }

    private int getLayerIndexOrThrow(T param0) {
        int var0 = this.keys.indexOf(param0);
        if (var0 == -1) {
            throw new IllegalStateException("Can't find " + param0 + " inside " + this.keys);
        } else {
            return var0;
        }
    }

    public RegistryAccess.Frozen getLayer(T param0) {
        int var0 = this.getLayerIndexOrThrow(param0);
        return this.values.get(var0);
    }

    public RegistryAccess.Frozen getAccessForLoading(T param0) {
        int var0 = this.getLayerIndexOrThrow(param0);
        return this.getCompositeAccessForLayers(0, var0);
    }

    public RegistryAccess.Frozen getAccessFrom(T param0) {
        int var0 = this.getLayerIndexOrThrow(param0);
        return this.getCompositeAccessForLayers(var0, this.values.size());
    }

    private RegistryAccess.Frozen getCompositeAccessForLayers(int param0, int param1) {
        return new RegistryAccess.ImmutableRegistryAccess(collectRegistries(this.values.subList(param0, param1).stream())).freeze();
    }

    public LayeredRegistryAccess<T> replaceFrom(T param0, RegistryAccess.Frozen... param1) {
        return this.replaceFrom(param0, Arrays.asList(param1));
    }

    public LayeredRegistryAccess<T> replaceFrom(T param0, List<RegistryAccess.Frozen> param1) {
        int var0 = this.getLayerIndexOrThrow(param0);
        if (param1.size() > this.values.size() - var0) {
            throw new IllegalStateException("Too many values to replace");
        } else {
            List<RegistryAccess.Frozen> var1 = new ArrayList<>();

            for(int var2 = 0; var2 < var0; ++var2) {
                var1.add(this.values.get(var2));
            }

            var1.addAll(param1);

            while(var1.size() < this.values.size()) {
                var1.add(RegistryAccess.EMPTY);
            }

            return new LayeredRegistryAccess<>(this.keys, var1);
        }
    }

    public RegistryAccess.Frozen compositeAccess() {
        return this.composite;
    }

    private static Map<ResourceKey<? extends Registry<?>>, Registry<?>> collectRegistries(Stream<? extends RegistryAccess> param0) {
        Map<ResourceKey<? extends Registry<?>>, Registry<?>> var0 = new HashMap<>();
        param0.forEach(param1 -> param1.registries().forEach(param1x -> {
                if (var0.put(param1x.key(), param1x.value()) != null) {
                    throw new IllegalStateException("Duplicated registry " + param1x.key());
                }
            }));
        return var0;
    }
}

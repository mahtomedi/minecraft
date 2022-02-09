package net.minecraft.server.packs.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;

public class MultiPackResourceManager implements CloseableResourceManager {
    private final Map<String, FallbackResourceManager> namespacedManagers;
    private final List<PackResources> packs;

    public MultiPackResourceManager(PackType param0, List<PackResources> param1) {
        this.packs = List.copyOf(param1);
        Map<String, FallbackResourceManager> var0 = new HashMap<>();

        for(PackResources var1 : param1) {
            for(String var2 : var1.getNamespaces(param0)) {
                var0.computeIfAbsent(var2, param1x -> new FallbackResourceManager(param0, param1x)).add(var1);
            }
        }

        this.namespacedManagers = var0;
    }

    @Override
    public Set<String> getNamespaces() {
        return this.namespacedManagers.keySet();
    }

    @Override
    public Resource getResource(ResourceLocation param0) throws IOException {
        ResourceManager var0 = this.namespacedManagers.get(param0.getNamespace());
        if (var0 != null) {
            return var0.getResource(param0);
        } else {
            throw new FileNotFoundException(param0.toString());
        }
    }

    @Override
    public boolean hasResource(ResourceLocation param0) {
        ResourceManager var0 = this.namespacedManagers.get(param0.getNamespace());
        return var0 != null ? var0.hasResource(param0) : false;
    }

    @Override
    public List<Resource> getResources(ResourceLocation param0) throws IOException {
        ResourceManager var0 = this.namespacedManagers.get(param0.getNamespace());
        if (var0 != null) {
            return var0.getResources(param0);
        } else {
            throw new FileNotFoundException(param0.toString());
        }
    }

    @Override
    public Collection<ResourceLocation> listResources(String param0, Predicate<String> param1) {
        Set<ResourceLocation> var0 = Sets.newHashSet();

        for(FallbackResourceManager var1 : this.namespacedManagers.values()) {
            var0.addAll(var1.listResources(param0, param1));
        }

        List<ResourceLocation> var2 = Lists.newArrayList(var0);
        Collections.sort(var2);
        return var2;
    }

    @Override
    public Stream<PackResources> listPacks() {
        return this.packs.stream();
    }

    @Override
    public void close() {
        this.packs.forEach(PackResources::close);
    }
}

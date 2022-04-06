package net.minecraft.server.packs.resources;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import org.slf4j.Logger;

public class FallbackResourceManager implements ResourceManager {
    static final Logger LOGGER = LogUtils.getLogger();
    protected final List<FallbackResourceManager.PackEntry> fallbacks = Lists.newArrayList();
    final PackType type;
    private final String namespace;

    public FallbackResourceManager(PackType param0, String param1) {
        this.type = param0;
        this.namespace = param1;
    }

    public void push(PackResources param0) {
        this.pushInternal(param0.getName(), param0, null);
    }

    public void push(PackResources param0, Predicate<ResourceLocation> param1) {
        this.pushInternal(param0.getName(), param0, param1);
    }

    public void pushFilterOnly(String param0, Predicate<ResourceLocation> param1) {
        this.pushInternal(param0, null, param1);
    }

    private void pushInternal(String param0, @Nullable PackResources param1, @Nullable Predicate<ResourceLocation> param2) {
        this.fallbacks.add(new FallbackResourceManager.PackEntry(param0, param1, param2));
    }

    @Override
    public Set<String> getNamespaces() {
        return ImmutableSet.of(this.namespace);
    }

    @Override
    public Optional<Resource> getResource(ResourceLocation param0) {
        if (!this.isValidLocation(param0)) {
            return Optional.empty();
        } else {
            for(int var0 = this.fallbacks.size() - 1; var0 >= 0; --var0) {
                FallbackResourceManager.PackEntry var1 = this.fallbacks.get(var0);
                PackResources var2 = var1.resources;
                if (var2 != null && var2.hasResource(this.type, param0)) {
                    return Optional.of(new Resource(var2.getName(), this.createResourceGetter(param0, var2), this.createStackMetadataFinder(param0, var0)));
                }

                if (var1.isFiltered(param0)) {
                    LOGGER.warn("Resource {} not found, but was filtered by pack {}", param0, var1.name);
                    return Optional.empty();
                }
            }

            return Optional.empty();
        }
    }

    Resource.IoSupplier<InputStream> createResourceGetter(ResourceLocation param0, PackResources param1) {
        return LOGGER.isDebugEnabled() ? () -> {
            InputStream var0 = param1.getResource(this.type, param0);
            return new FallbackResourceManager.LeakedResourceWarningInputStream(var0, param0, param1.getName());
        } : () -> param1.getResource(this.type, param0);
    }

    private boolean isValidLocation(ResourceLocation param0) {
        return !param0.getPath().contains("..");
    }

    @Override
    public List<Resource> getResourceStack(ResourceLocation param0) {
        if (!this.isValidLocation(param0)) {
            return List.of();
        } else {
            List<FallbackResourceManager.SinglePackResourceThunkSupplier> var0 = Lists.newArrayList();
            ResourceLocation var1 = getMetadataLocation(param0);
            String var2 = null;

            for(FallbackResourceManager.PackEntry var3 : this.fallbacks) {
                if (var3.isFiltered(param0)) {
                    if (!var0.isEmpty()) {
                        var2 = var3.name;
                    }

                    var0.clear();
                } else if (var3.isFiltered(var1)) {
                    var0.forEach(FallbackResourceManager.SinglePackResourceThunkSupplier::ignoreMeta);
                }

                PackResources var4 = var3.resources;
                if (var4 != null && var4.hasResource(this.type, param0)) {
                    var0.add(new FallbackResourceManager.SinglePackResourceThunkSupplier(param0, var1, var4));
                }
            }

            if (var0.isEmpty() && var2 != null) {
                LOGGER.info("Resource {} was filtered by pack {}", param0, var2);
            }

            return var0.stream().map(FallbackResourceManager.SinglePackResourceThunkSupplier::create).toList();
        }
    }

    @Override
    public Map<ResourceLocation, Resource> listResources(String param0, Predicate<ResourceLocation> param1) {
        Object2IntMap<ResourceLocation> var0 = new Object2IntOpenHashMap<>();
        int var1 = this.fallbacks.size();

        for(int var2 = 0; var2 < var1; ++var2) {
            FallbackResourceManager.PackEntry var3 = this.fallbacks.get(var2);
            var3.filterAll(var0.keySet());
            if (var3.resources != null) {
                for(ResourceLocation var4 : var3.resources.getResources(this.type, this.namespace, param0, param1)) {
                    var0.put(var4, var2);
                }
            }
        }

        Map<ResourceLocation, Resource> var5 = Maps.newTreeMap();

        for(Entry<ResourceLocation> var6 : Object2IntMaps.fastIterable(var0)) {
            int var7 = var6.getIntValue();
            ResourceLocation var8 = var6.getKey();
            PackResources var9 = this.fallbacks.get(var7).resources;
            var5.put(var8, new Resource(var9.getName(), this.createResourceGetter(var8, var9), this.createStackMetadataFinder(var8, var7)));
        }

        return var5;
    }

    private Resource.IoSupplier<ResourceMetadata> createStackMetadataFinder(ResourceLocation param0, int param1) {
        return () -> {
            ResourceLocation var0 = getMetadataLocation(param0);

            for(int var1x = this.fallbacks.size() - 1; var1x >= param1; --var1x) {
                FallbackResourceManager.PackEntry var2x = this.fallbacks.get(var1x);
                PackResources var3 = var2x.resources;
                if (var3 != null && var3.hasResource(this.type, var0)) {
                    ResourceMetadata var8;
                    try (InputStream var4 = var3.getResource(this.type, var0)) {
                        var8 = ResourceMetadata.fromJsonStream(var4);
                    }

                    return var8;
                }

                if (var2x.isFiltered(var0)) {
                    break;
                }
            }

            return ResourceMetadata.EMPTY;
        };
    }

    private static void applyPackFiltersToExistingResources(
        FallbackResourceManager.PackEntry param0, Map<ResourceLocation, FallbackResourceManager.EntryStack> param1
    ) {
        Iterator<java.util.Map.Entry<ResourceLocation, FallbackResourceManager.EntryStack>> var0 = param1.entrySet().iterator();

        while(var0.hasNext()) {
            java.util.Map.Entry<ResourceLocation, FallbackResourceManager.EntryStack> var1 = var0.next();
            ResourceLocation var2 = var1.getKey();
            FallbackResourceManager.EntryStack var3 = var1.getValue();
            if (param0.isFiltered(var2)) {
                var0.remove();
            } else if (param0.isFiltered(var3.metadataLocation())) {
                var3.entries.forEach(FallbackResourceManager.SinglePackResourceThunkSupplier::ignoreMeta);
            }
        }

    }

    private void listPackResources(
        FallbackResourceManager.PackEntry param0,
        String param1,
        Predicate<ResourceLocation> param2,
        Map<ResourceLocation, FallbackResourceManager.EntryStack> param3
    ) {
        PackResources var0 = param0.resources;
        if (var0 != null) {
            for(ResourceLocation var1 : var0.getResources(this.type, this.namespace, param1, param2)) {
                ResourceLocation var2 = getMetadataLocation(var1);
                param3.computeIfAbsent(var1, param1x -> new FallbackResourceManager.EntryStack(var2, Lists.newArrayList()))
                    .entries()
                    .add(new FallbackResourceManager.SinglePackResourceThunkSupplier(var1, var2, var0));
            }

        }
    }

    @Override
    public Map<ResourceLocation, List<Resource>> listResourceStacks(String param0, Predicate<ResourceLocation> param1) {
        Map<ResourceLocation, FallbackResourceManager.EntryStack> var0 = Maps.newHashMap();

        for(FallbackResourceManager.PackEntry var1 : this.fallbacks) {
            applyPackFiltersToExistingResources(var1, var0);
            this.listPackResources(var1, param0, param1, var0);
        }

        TreeMap<ResourceLocation, List<Resource>> var2 = Maps.newTreeMap();
        var0.forEach((param1x, param2) -> var2.put(param1x, param2.createThunks()));
        return var2;
    }

    @Override
    public Stream<PackResources> listPacks() {
        return this.fallbacks.stream().map(param0 -> param0.resources).filter(Objects::nonNull);
    }

    static ResourceLocation getMetadataLocation(ResourceLocation param0) {
        return new ResourceLocation(param0.getNamespace(), param0.getPath() + ".mcmeta");
    }

    static record EntryStack(ResourceLocation metadataLocation, List<FallbackResourceManager.SinglePackResourceThunkSupplier> entries) {
        List<Resource> createThunks() {
            return this.entries().stream().map(FallbackResourceManager.SinglePackResourceThunkSupplier::create).toList();
        }
    }

    static class LeakedResourceWarningInputStream extends FilterInputStream {
        private final String message;
        private boolean closed;

        public LeakedResourceWarningInputStream(InputStream param0, ResourceLocation param1, String param2) {
            super(param0);
            ByteArrayOutputStream var0 = new ByteArrayOutputStream();
            new Exception().printStackTrace(new PrintStream(var0));
            this.message = "Leaked resource: '" + param1 + "' loaded from pack: '" + param2 + "'\n" + var0;
        }

        @Override
        public void close() throws IOException {
            super.close();
            this.closed = true;
        }

        @Override
        protected void finalize() throws Throwable {
            if (!this.closed) {
                FallbackResourceManager.LOGGER.warn(this.message);
            }

            super.finalize();
        }
    }

    static record PackEntry(String name, @Nullable PackResources resources, @Nullable Predicate<ResourceLocation> filter) {
        public void filterAll(Collection<ResourceLocation> param0) {
            if (this.filter != null) {
                param0.removeIf(this.filter);
            }

        }

        public boolean isFiltered(ResourceLocation param0) {
            return this.filter != null && this.filter.test(param0);
        }
    }

    class SinglePackResourceThunkSupplier {
        private final ResourceLocation location;
        private final ResourceLocation metadataLocation;
        private final PackResources source;
        private boolean shouldGetMeta = true;

        SinglePackResourceThunkSupplier(ResourceLocation param0, ResourceLocation param1, PackResources param2) {
            this.source = param2;
            this.location = param0;
            this.metadataLocation = param1;
        }

        public void ignoreMeta() {
            this.shouldGetMeta = false;
        }

        public Resource create() {
            String var0 = this.source.getName();
            return this.shouldGetMeta ? new Resource(var0, FallbackResourceManager.this.createResourceGetter(this.location, this.source), () -> {
                if (this.source.hasResource(FallbackResourceManager.this.type, this.metadataLocation)) {
                    ResourceMetadata var2;
                    try (InputStream var0x = this.source.getResource(FallbackResourceManager.this.type, this.metadataLocation)) {
                        var2 = ResourceMetadata.fromJsonStream(var0x);
                    }

                    return var2;
                } else {
                    return ResourceMetadata.EMPTY;
                }
            }) : new Resource(var0, FallbackResourceManager.this.createResourceGetter(this.location, this.source));
        }
    }
}

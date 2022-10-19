package net.minecraft.server.packs.resources;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import org.slf4j.Logger;

public class FallbackResourceManager implements ResourceManager {
    static final Logger LOGGER = LogUtils.getLogger();
    protected final List<FallbackResourceManager.PackEntry> fallbacks = Lists.newArrayList();
    private final PackType type;
    private final String namespace;

    public FallbackResourceManager(PackType param0, String param1) {
        this.type = param0;
        this.namespace = param1;
    }

    public void push(PackResources param0) {
        this.pushInternal(param0.packId(), param0, null);
    }

    public void push(PackResources param0, Predicate<ResourceLocation> param1) {
        this.pushInternal(param0.packId(), param0, param1);
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
        for(int var0 = this.fallbacks.size() - 1; var0 >= 0; --var0) {
            FallbackResourceManager.PackEntry var1 = this.fallbacks.get(var0);
            PackResources var2 = var1.resources;
            if (var2 != null) {
                IoSupplier<InputStream> var3 = var2.getResource(this.type, param0);
                if (var3 != null) {
                    IoSupplier<ResourceMetadata> var4 = this.createStackMetadataFinder(param0, var0);
                    return Optional.of(createResource(var2, param0, var3, var4));
                }
            }

            if (var1.isFiltered(param0)) {
                LOGGER.warn("Resource {} not found, but was filtered by pack {}", param0, var1.name);
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    private static Resource createResource(PackResources param0, ResourceLocation param1, IoSupplier<InputStream> param2, IoSupplier<ResourceMetadata> param3) {
        return new Resource(param0, wrapForDebug(param1, param0, param2), param3);
    }

    private static IoSupplier<InputStream> wrapForDebug(ResourceLocation param0, PackResources param1, IoSupplier<InputStream> param2) {
        return LOGGER.isDebugEnabled() ? () -> new FallbackResourceManager.LeakedResourceWarningInputStream(param2.get(), param0, param1.packId()) : param2;
    }

    @Override
    public List<Resource> getResourceStack(ResourceLocation param0) {
        ResourceLocation var0 = getMetadataLocation(param0);
        List<Resource> var1 = new ArrayList<>();
        boolean var2 = false;
        String var3 = null;

        for(int var4 = this.fallbacks.size() - 1; var4 >= 0; --var4) {
            FallbackResourceManager.PackEntry var5 = this.fallbacks.get(var4);
            PackResources var6 = var5.resources;
            if (var6 != null) {
                IoSupplier<InputStream> var7 = var6.getResource(this.type, param0);
                if (var7 != null) {
                    IoSupplier<ResourceMetadata> var8;
                    if (var2) {
                        var8 = ResourceMetadata.EMPTY_SUPPLIER;
                    } else {
                        var8 = () -> {
                            IoSupplier<InputStream> var0x = var6.getResource(this.type, var0);
                            return var0x != null ? parseMetadata(var0x) : ResourceMetadata.EMPTY;
                        };
                    }

                    var1.add(new Resource(var6, var7, var8));
                }
            }

            if (var5.isFiltered(param0)) {
                var3 = var5.name;
                break;
            }

            if (var5.isFiltered(var0)) {
                var2 = true;
            }
        }

        if (var1.isEmpty() && var3 != null) {
            LOGGER.warn("Resource {} not found, but was filtered by pack {}", param0, var3);
        }

        return Lists.reverse(var1);
    }

    private static boolean isMetadata(ResourceLocation param0) {
        return param0.getPath().endsWith(".mcmeta");
    }

    private static ResourceLocation getResourceLocationFromMetadata(ResourceLocation param0) {
        String var0 = param0.getPath().substring(0, param0.getPath().length() - ".mcmeta".length());
        return param0.withPath(var0);
    }

    static ResourceLocation getMetadataLocation(ResourceLocation param0) {
        return param0.withPath(param0.getPath() + ".mcmeta");
    }

    @Override
    public Map<ResourceLocation, Resource> listResources(String param0, Predicate<ResourceLocation> param1) {
        record ResourceWithSourceAndIndex(PackResources packResources, IoSupplier<InputStream> resource, int packIndex) {
        }

        Map<ResourceLocation, ResourceWithSourceAndIndex> var0 = new HashMap<>();
        Map<ResourceLocation, ResourceWithSourceAndIndex> var1 = new HashMap<>();
        int var2 = this.fallbacks.size();

        for(int var3 = 0; var3 < var2; ++var3) {
            FallbackResourceManager.PackEntry var4 = this.fallbacks.get(var3);
            var4.filterAll(var0.keySet());
            var4.filterAll(var1.keySet());
            PackResources var5 = var4.resources;
            if (var5 != null) {
                int var6 = var3;
                var5.listResources(this.type, this.namespace, param0, (param5, param6) -> {
                    if (isMetadata(param5)) {
                        if (param1.test(getResourceLocationFromMetadata(param5))) {
                            var1.put(param5, new ResourceWithSourceAndIndex(var5, param6, var6));
                        }
                    } else if (param1.test(param5)) {
                        var0.put(param5, new ResourceWithSourceAndIndex(var5, param6, var6));
                    }

                });
            }
        }

        Map<ResourceLocation, Resource> var7 = Maps.newTreeMap();
        var0.forEach((param2, param3) -> {
            ResourceLocation var0x = getMetadataLocation(param2);
            ResourceWithSourceAndIndex var1x = var1.get(var0x);
            IoSupplier<ResourceMetadata> var2x;
            if (var1x != null && var1x.packIndex >= param3.packIndex) {
                var2x = convertToMetadata(var1x.resource);
            } else {
                var2x = ResourceMetadata.EMPTY_SUPPLIER;
            }

            var7.put(param2, createResource(param3.packResources, param2, param3.resource, var2x));
        });
        return var7;
    }

    private IoSupplier<ResourceMetadata> createStackMetadataFinder(ResourceLocation param0, int param1) {
        return () -> {
            ResourceLocation var0 = getMetadataLocation(param0);

            for(int var1x = this.fallbacks.size() - 1; var1x >= param1; --var1x) {
                FallbackResourceManager.PackEntry var2x = this.fallbacks.get(var1x);
                PackResources var3 = var2x.resources;
                if (var3 != null) {
                    IoSupplier<InputStream> var4 = var3.getResource(this.type, var0);
                    if (var4 != null) {
                        return parseMetadata(var4);
                    }
                }

                if (var2x.isFiltered(var0)) {
                    break;
                }
            }

            return ResourceMetadata.EMPTY;
        };
    }

    private static IoSupplier<ResourceMetadata> convertToMetadata(IoSupplier<InputStream> param0) {
        return () -> parseMetadata(param0);
    }

    private static ResourceMetadata parseMetadata(IoSupplier<InputStream> param0) throws IOException {
        ResourceMetadata var2;
        try (InputStream var0 = param0.get()) {
            var2 = ResourceMetadata.fromJsonStream(var0);
        }

        return var2;
    }

    private static void applyPackFiltersToExistingResources(
        FallbackResourceManager.PackEntry param0, Map<ResourceLocation, FallbackResourceManager.EntryStack> param1
    ) {
        for(FallbackResourceManager.EntryStack var0 : param1.values()) {
            if (param0.isFiltered(var0.fileLocation)) {
                var0.fileSources.clear();
            } else if (param0.isFiltered(var0.metadataLocation())) {
                var0.metaSources.clear();
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
            var0.listResources(
                this.type,
                this.namespace,
                param1,
                (param3x, param4) -> {
                    if (isMetadata(param3x)) {
                        ResourceLocation var0x = getResourceLocationFromMetadata(param3x);
                        if (!param2.test(var0x)) {
                            return;
                        }
    
                        param3.computeIfAbsent(var0x, FallbackResourceManager.EntryStack::new).metaSources.put(var0, param4);
                    } else {
                        if (!param2.test(param3x)) {
                            return;
                        }
    
                        param3.computeIfAbsent(param3x, FallbackResourceManager.EntryStack::new)
                            .fileSources
                            .add(new FallbackResourceManager.ResourceWithSource(var0, param4));
                    }
    
                }
            );
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

        for(FallbackResourceManager.EntryStack var3 : var0.values()) {
            if (!var3.fileSources.isEmpty()) {
                List<Resource> var4 = new ArrayList<>();

                for(FallbackResourceManager.ResourceWithSource var5 : var3.fileSources) {
                    PackResources var6 = var5.source;
                    IoSupplier<InputStream> var7 = var3.metaSources.get(var6);
                    IoSupplier<ResourceMetadata> var8 = var7 != null ? convertToMetadata(var7) : ResourceMetadata.EMPTY_SUPPLIER;
                    var4.add(createResource(var6, var3.fileLocation, var5.resource, var8));
                }

                var2.put(var3.fileLocation, var4);
            }
        }

        return var2;
    }

    @Override
    public Stream<PackResources> listPacks() {
        return this.fallbacks.stream().map(param0 -> param0.resources).filter(Objects::nonNull);
    }

    static record EntryStack(
        ResourceLocation fileLocation,
        ResourceLocation metadataLocation,
        List<FallbackResourceManager.ResourceWithSource> fileSources,
        Map<PackResources, IoSupplier<InputStream>> metaSources
    ) {
        EntryStack(ResourceLocation param0) {
            this(param0, FallbackResourceManager.getMetadataLocation(param0), new ArrayList<>(), new Object2ObjectArrayMap<>());
        }
    }

    static class LeakedResourceWarningInputStream extends FilterInputStream {
        private final Supplier<String> message;
        private boolean closed;

        public LeakedResourceWarningInputStream(InputStream param0, ResourceLocation param1, String param2) {
            super(param0);
            Exception var0 = new Exception("Stacktrace");
            this.message = () -> {
                StringWriter var0x = new StringWriter();
                var0.printStackTrace(new PrintWriter(var0x));
                return "Leaked resource: '" + param1 + "' loaded from pack: '" + param2 + "'\n" + var0x;
            };
        }

        @Override
        public void close() throws IOException {
            super.close();
            this.closed = true;
        }

        @Override
        protected void finalize() throws Throwable {
            if (!this.closed) {
                FallbackResourceManager.LOGGER.warn("{}", this.message.get());
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

    static record ResourceWithSource(PackResources source, IoSupplier<InputStream> resource) {
    }
}

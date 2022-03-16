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
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    public Resource getResource(ResourceLocation param0) throws IOException {
        this.validateLocation(param0);
        PackResources var0 = null;
        ResourceLocation var1 = getMetadataLocation(param0);
        boolean var2 = false;

        for(FallbackResourceManager.PackEntry var3 : Lists.reverse(this.fallbacks)) {
            PackResources var4 = var3.resources;
            if (var4 != null) {
                if (!var2) {
                    if (var4.hasResource(this.type, var1)) {
                        var0 = var4;
                        var2 = true;
                    } else {
                        var2 = var3.isFiltered(var1);
                    }
                }

                if (var4.hasResource(this.type, param0)) {
                    InputStream var5 = null;
                    if (var0 != null) {
                        var5 = this.getWrappedResource(var1, var0);
                    }

                    return new SimpleResource(var4.getName(), param0, this.getWrappedResource(param0, var4), var5);
                }
            }

            if (var3.isFiltered(param0)) {
                throw new FileNotFoundException(param0 + " (filtered by: " + var3.name + ")");
            }
        }

        throw new FileNotFoundException(param0.toString());
    }

    @Override
    public boolean hasResource(ResourceLocation param0) {
        if (!this.isValidLocation(param0)) {
            return false;
        } else {
            for(FallbackResourceManager.PackEntry var0 : Lists.reverse(this.fallbacks)) {
                if (var0.hasResource(this.type, param0)) {
                    return true;
                }

                if (var0.isFiltered(param0)) {
                    return false;
                }
            }

            return false;
        }
    }

    protected InputStream getWrappedResource(ResourceLocation param0, PackResources param1) throws IOException {
        InputStream var0 = param1.getResource(this.type, param0);
        return (InputStream)(LOGGER.isDebugEnabled() ? new FallbackResourceManager.LeakedResourceWarningInputStream(var0, param0, param1.getName()) : var0);
    }

    private void validateLocation(ResourceLocation param0) throws IOException {
        if (!this.isValidLocation(param0)) {
            throw new IOException("Invalid relative path to resource: " + param0);
        }
    }

    private boolean isValidLocation(ResourceLocation param0) {
        return !param0.getPath().contains("..");
    }

    @Override
    public List<ResourceThunk> getResourceStack(ResourceLocation param0) throws IOException {
        this.validateLocation(param0);
        List<FallbackResourceManager.SinglePackResourceThunkSupplier> var0 = Lists.newArrayList();
        ResourceLocation var1 = getMetadataLocation(param0);
        String var2 = null;

        for(FallbackResourceManager.PackEntry var3 : this.fallbacks) {
            if (var3.isFiltered(param0)) {
                var0.clear();
                var2 = var3.name;
            } else if (var3.isFiltered(var1)) {
                var0.forEach(FallbackResourceManager.SinglePackResourceThunkSupplier::ignoreMeta);
            }

            PackResources var4 = var3.resources;
            if (var4 != null && var4.hasResource(this.type, param0)) {
                var0.add(new FallbackResourceManager.SinglePackResourceThunkSupplier(param0, var1, var4));
            }
        }

        if (!var0.isEmpty()) {
            return var0.stream().map(FallbackResourceManager.SinglePackResourceThunkSupplier::create).toList();
        } else if (var2 != null) {
            throw new FileNotFoundException(param0 + " (filtered by: " + var2 + ")");
        } else {
            throw new FileNotFoundException(param0.toString());
        }
    }

    @Override
    public Map<ResourceLocation, ResourceThunk> listResources(String param0, Predicate<ResourceLocation> param1) {
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

        Map<ResourceLocation, ResourceThunk> var5 = Maps.newTreeMap();

        for(Entry<ResourceLocation> var6 : Object2IntMaps.fastIterable(var0)) {
            int var7 = var6.getIntValue();
            ResourceLocation var8 = var6.getKey();
            PackResources var9 = this.fallbacks.get(var7).resources;
            String var10 = var9.getName();
            var5.put(var8, new ResourceThunk(var10, () -> {
                ResourceLocation var0x = getMetadataLocation(var8);
                InputStream var1x = null;

                for(int var2x = this.fallbacks.size() - 1; var2x >= var7; --var2x) {
                    FallbackResourceManager.PackEntry var3x = this.fallbacks.get(var2x);
                    PackResources var4x = var3x.resources;
                    if (var4x != null && var4x.hasResource(this.type, var0x)) {
                        var1x = this.getWrappedResource(var0x, var4x);
                        break;
                    }

                    if (var3x.isFiltered(var0x)) {
                        break;
                    }
                }

                return new SimpleResource(var10, var8, this.getWrappedResource(var8, var9), var1x);
            }));
        }

        return var5;
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
    public Map<ResourceLocation, List<ResourceThunk>> listResourceStacks(String param0, Predicate<ResourceLocation> param1) {
        Map<ResourceLocation, FallbackResourceManager.EntryStack> var0 = Maps.newHashMap();

        for(FallbackResourceManager.PackEntry var1 : this.fallbacks) {
            applyPackFiltersToExistingResources(var1, var0);
            this.listPackResources(var1, param0, param1, var0);
        }

        TreeMap<ResourceLocation, List<ResourceThunk>> var2 = Maps.newTreeMap();
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
        List<ResourceThunk> createThunks() {
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

        boolean hasResource(PackType param0, ResourceLocation param1) {
            return this.resources != null && this.resources.hasResource(param0, param1);
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

        public ResourceThunk create() {
            String var0 = this.source.getName();
            return this.shouldGetMeta
                ? new ResourceThunk(
                    var0,
                    () -> {
                        InputStream var0x = this.source.hasResource(FallbackResourceManager.this.type, this.metadataLocation)
                            ? FallbackResourceManager.this.getWrappedResource(this.metadataLocation, this.source)
                            : null;
                        return new SimpleResource(var0, this.location, FallbackResourceManager.this.getWrappedResource(this.location, this.source), var0x);
                    }
                )
                : new ResourceThunk(
                    var0, () -> new SimpleResource(var0, this.location, FallbackResourceManager.this.getWrappedResource(this.location, this.source), null)
                );
        }
    }
}

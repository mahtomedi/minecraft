package net.minecraft.server.packs.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.Unit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SimpleReloadableResourceManager implements ReloadableResourceManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Map<String, FallbackResourceManager> namespacedPacks = Maps.newHashMap();
    private final List<PreparableReloadListener> listeners = Lists.newArrayList();
    private final Set<String> namespaces = Sets.newLinkedHashSet();
    private final List<PackResources> packs = Lists.newArrayList();
    private final PackType type;

    public SimpleReloadableResourceManager(PackType param0) {
        this.type = param0;
    }

    public void add(PackResources param0) {
        this.packs.add(param0);

        for(String var0 : param0.getNamespaces(this.type)) {
            this.namespaces.add(var0);
            FallbackResourceManager var1 = this.namespacedPacks.get(var0);
            if (var1 == null) {
                var1 = new FallbackResourceManager(this.type, var0);
                this.namespacedPacks.put(var0, var1);
            }

            var1.add(param0);
        }

    }

    @Override
    public Set<String> getNamespaces() {
        return this.namespaces;
    }

    @Override
    public Resource getResource(ResourceLocation param0) throws IOException {
        ResourceManager var0 = this.namespacedPacks.get(param0.getNamespace());
        if (var0 != null) {
            return var0.getResource(param0);
        } else {
            throw new FileNotFoundException(param0.toString());
        }
    }

    @Override
    public boolean hasResource(ResourceLocation param0) {
        ResourceManager var0 = this.namespacedPacks.get(param0.getNamespace());
        return var0 != null ? var0.hasResource(param0) : false;
    }

    @Override
    public List<Resource> getResources(ResourceLocation param0) throws IOException {
        ResourceManager var0 = this.namespacedPacks.get(param0.getNamespace());
        if (var0 != null) {
            return var0.getResources(param0);
        } else {
            throw new FileNotFoundException(param0.toString());
        }
    }

    @Override
    public Collection<ResourceLocation> listResources(String param0, Predicate<String> param1) {
        Set<ResourceLocation> var0 = Sets.newHashSet();

        for(FallbackResourceManager var1 : this.namespacedPacks.values()) {
            var0.addAll(var1.listResources(param0, param1));
        }

        List<ResourceLocation> var2 = Lists.newArrayList(var0);
        Collections.sort(var2);
        return var2;
    }

    private void clear() {
        this.namespacedPacks.clear();
        this.namespaces.clear();
        this.packs.forEach(PackResources::close);
        this.packs.clear();
    }

    @Override
    public void close() {
        this.clear();
    }

    @Override
    public void registerReloadListener(PreparableReloadListener param0) {
        this.listeners.add(param0);
    }

    @Override
    public ReloadInstance createReload(Executor param0, Executor param1, CompletableFuture<Unit> param2, List<PackResources> param3) {
        LOGGER.info("Reloading ResourceManager: {}", () -> param3.stream().map(PackResources::getName).collect(Collectors.joining(", ")));
        this.clear();

        for(PackResources var0 : param3) {
            try {
                this.add(var0);
            } catch (Exception var8) {
                LOGGER.error("Failed to add resource pack {}", var0.getName(), var8);
                return new SimpleReloadableResourceManager.FailingReloadInstance(new SimpleReloadableResourceManager.ResourcePackLoadingFailure(var0, var8));
            }
        }

        return (ReloadInstance)(LOGGER.isDebugEnabled()
            ? new ProfiledReloadInstance(this, Lists.newArrayList(this.listeners), param0, param1, param2)
            : SimpleReloadInstance.of(this, Lists.newArrayList(this.listeners), param0, param1, param2));
    }

    @Override
    public Stream<PackResources> listPacks() {
        return this.packs.stream();
    }

    static class FailingReloadInstance implements ReloadInstance {
        private final SimpleReloadableResourceManager.ResourcePackLoadingFailure exception;
        private final CompletableFuture<Unit> failedFuture;

        public FailingReloadInstance(SimpleReloadableResourceManager.ResourcePackLoadingFailure param0) {
            this.exception = param0;
            this.failedFuture = new CompletableFuture<>();
            this.failedFuture.completeExceptionally(param0);
        }

        @Override
        public CompletableFuture<Unit> done() {
            return this.failedFuture;
        }

        @Override
        public float getActualProgress() {
            return 0.0F;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public void checkExceptions() {
            throw this.exception;
        }
    }

    public static class ResourcePackLoadingFailure extends RuntimeException {
        private final PackResources pack;

        public ResourcePackLoadingFailure(PackResources param0, Throwable param1) {
            super(param0.getName(), param1);
            this.pack = param0;
        }

        public PackResources getPack() {
            return this.pack;
        }
    }
}

package net.minecraft.server.packs.resources;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.io.IOException;
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
import org.slf4j.Logger;

public class ReloadableResourceManager implements AutoCloseable, ResourceManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private CloseableResourceManager resources;
    private final List<PreparableReloadListener> listeners = Lists.newArrayList();
    private final PackType type;

    public ReloadableResourceManager(PackType param0) {
        this.type = param0;
        this.resources = new MultiPackResourceManager(param0, List.of());
    }

    @Override
    public void close() {
        this.resources.close();
    }

    public void registerReloadListener(PreparableReloadListener param0) {
        this.listeners.add(param0);
    }

    public ReloadInstance createReload(Executor param0, Executor param1, CompletableFuture<Unit> param2, List<PackResources> param3) {
        LOGGER.info("Reloading ResourceManager: {}", LogUtils.defer(() -> param3.stream().map(PackResources::getName).collect(Collectors.joining(", "))));
        this.resources.close();
        this.resources = new MultiPackResourceManager(this.type, param3);
        return SimpleReloadInstance.create(this.resources, this.listeners, param0, param1, param2, LOGGER.isDebugEnabled());
    }

    @Override
    public Resource getResource(ResourceLocation param0) throws IOException {
        return this.resources.getResource(param0);
    }

    @Override
    public Set<String> getNamespaces() {
        return this.resources.getNamespaces();
    }

    @Override
    public boolean hasResource(ResourceLocation param0) {
        return this.resources.hasResource(param0);
    }

    @Override
    public List<ResourceThunk> getResourceStack(ResourceLocation param0) throws IOException {
        return this.resources.getResourceStack(param0);
    }

    @Override
    public Map<ResourceLocation, ResourceThunk> listResources(String param0, Predicate<ResourceLocation> param1) {
        return this.resources.listResources(param0, param1);
    }

    @Override
    public Map<ResourceLocation, List<ResourceThunk>> listResourceStacks(String param0, Predicate<ResourceLocation> param1) {
        return this.resources.listResourceStacks(param0, param1);
    }

    @Override
    public Stream<PackResources> listPacks() {
        return this.resources.listPacks();
    }
}

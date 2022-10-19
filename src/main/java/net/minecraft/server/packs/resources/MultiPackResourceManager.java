package net.minecraft.server.packs.resources;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

public class MultiPackResourceManager implements CloseableResourceManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<String, FallbackResourceManager> namespacedManagers;
    private final List<PackResources> packs;

    public MultiPackResourceManager(PackType param0, List<PackResources> param1) {
        this.packs = List.copyOf(param1);
        Map<String, FallbackResourceManager> var0 = new HashMap<>();
        List<String> var1 = param1.stream().flatMap(param1x -> param1x.getNamespaces(param0).stream()).distinct().toList();

        for(PackResources var2 : param1) {
            ResourceFilterSection var3 = this.getPackFilterSection(var2);
            Set<String> var4 = var2.getNamespaces(param0);
            Predicate<ResourceLocation> var5 = var3 != null ? param1x -> var3.isPathFiltered(param1x.getPath()) : null;

            for(String var6 : var1) {
                boolean var7 = var4.contains(var6);
                boolean var8 = var3 != null && var3.isNamespaceFiltered(var6);
                if (var7 || var8) {
                    FallbackResourceManager var9 = var0.get(var6);
                    if (var9 == null) {
                        var9 = new FallbackResourceManager(param0, var6);
                        var0.put(var6, var9);
                    }

                    if (var7 && var8) {
                        var9.push(var2, var5);
                    } else if (var7) {
                        var9.push(var2);
                    } else {
                        var9.pushFilterOnly(var2.packId(), var5);
                    }
                }
            }
        }

        this.namespacedManagers = var0;
    }

    @Nullable
    private ResourceFilterSection getPackFilterSection(PackResources param0) {
        try {
            return param0.getMetadataSection(ResourceFilterSection.TYPE);
        } catch (IOException var3) {
            LOGGER.error("Failed to get filter section from pack {}", param0.packId());
            return null;
        }
    }

    @Override
    public Set<String> getNamespaces() {
        return this.namespacedManagers.keySet();
    }

    @Override
    public Optional<Resource> getResource(ResourceLocation param0) {
        ResourceManager var0 = this.namespacedManagers.get(param0.getNamespace());
        return var0 != null ? var0.getResource(param0) : Optional.empty();
    }

    @Override
    public List<Resource> getResourceStack(ResourceLocation param0) {
        ResourceManager var0 = this.namespacedManagers.get(param0.getNamespace());
        return var0 != null ? var0.getResourceStack(param0) : List.of();
    }

    @Override
    public Map<ResourceLocation, Resource> listResources(String param0, Predicate<ResourceLocation> param1) {
        checkTrailingDirectoryPath(param0);
        Map<ResourceLocation, Resource> var0 = new TreeMap<>();

        for(FallbackResourceManager var1 : this.namespacedManagers.values()) {
            var0.putAll(var1.listResources(param0, param1));
        }

        return var0;
    }

    @Override
    public Map<ResourceLocation, List<Resource>> listResourceStacks(String param0, Predicate<ResourceLocation> param1) {
        checkTrailingDirectoryPath(param0);
        Map<ResourceLocation, List<Resource>> var0 = new TreeMap<>();

        for(FallbackResourceManager var1 : this.namespacedManagers.values()) {
            var0.putAll(var1.listResourceStacks(param0, param1));
        }

        return var0;
    }

    private static void checkTrailingDirectoryPath(String param0) {
        if (param0.endsWith("/")) {
            throw new IllegalArgumentException("Trailing slash in path " + param0);
        }
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

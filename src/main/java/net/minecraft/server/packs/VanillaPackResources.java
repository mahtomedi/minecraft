package net.minecraft.server.packs;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.slf4j.Logger;

public class VanillaPackResources implements PackResources {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final BuiltInMetadata metadata;
    private final Set<String> namespaces;
    private final List<Path> rootPaths;
    private final Map<PackType, List<Path>> pathsForType;

    VanillaPackResources(BuiltInMetadata param0, Set<String> param1, List<Path> param2, Map<PackType, List<Path>> param3) {
        this.metadata = param0;
        this.namespaces = param1;
        this.rootPaths = param2;
        this.pathsForType = param3;
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String... param0) {
        FileUtil.validatePath(param0);
        List<String> var0 = List.of(param0);

        for(Path var1 : this.rootPaths) {
            Path var2 = FileUtil.resolvePath(var1, var0);
            if (Files.exists(var2) && PathPackResources.validatePath(var2)) {
                return IoSupplier.create(var2);
            }
        }

        return null;
    }

    public void listRawPaths(PackType param0, ResourceLocation param1, Consumer<Path> param2) {
        FileUtil.decomposePath(param1.getPath()).get().ifLeft(param3 -> {
            String var0 = param1.getNamespace();

            for(Path var1x : this.pathsForType.get(param0)) {
                Path var2x = var1x.resolve(var0);
                param2.accept(FileUtil.resolvePath(var2x, param3));
            }

        }).ifRight(param1x -> LOGGER.error("Invalid path {}: {}", param1, param1x.message()));
    }

    @Override
    public void listResources(PackType param0, String param1, String param2, PackResources.ResourceOutput param3) {
        FileUtil.decomposePath(param2).get().ifLeft(param3x -> {
            List<Path> var0 = this.pathsForType.get(param0);
            int var1x = var0.size();
            if (var1x == 1) {
                getResources(param3, param1, var0.get(0), param3x);
            } else if (var1x > 1) {
                Map<ResourceLocation, IoSupplier<InputStream>> var2x = new HashMap();

                for(int var4x = 0; var4x < var1x - 1; ++var4x) {
                    getResources(var2x::putIfAbsent, param1, var0.get(var4x), param3x);
                }

                Path var4 = var0.get(var1x - 1);
                if (var2x.isEmpty()) {
                    getResources(param3, param1, var4, param3x);
                } else {
                    getResources(var2x::putIfAbsent, param1, var4, param3x);
                    var2x.forEach(param3);
                }
            }

        }).ifRight(param1x -> LOGGER.error("Invalid path {}: {}", param2, param1x.message()));
    }

    private static void getResources(PackResources.ResourceOutput param0, String param1, Path param2, List<String> param3) {
        Path var0 = param2.resolve(param1);
        PathPackResources.listPath(param1, var0, param3, param0);
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getResource(PackType param0, ResourceLocation param1) {
        return FileUtil.decomposePath(param1.getPath()).get().map(param2 -> {
            String var0 = param1.getNamespace();

            for(Path var1x : this.pathsForType.get(param0)) {
                Path var2x = FileUtil.resolvePath(var1x.resolve(var0), param2);
                if (Files.exists(var2x) && PathPackResources.validatePath(var2x)) {
                    return IoSupplier.create(var2x);
                }
            }

            return null;
        }, param1x -> {
            LOGGER.error("Invalid path {}: {}", param1, param1x.message());
            return null;
        });
    }

    @Override
    public Set<String> getNamespaces(PackType param0) {
        return this.namespaces;
    }

    @Nullable
    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> param0) {
        IoSupplier<InputStream> var0 = this.getRootResource("pack.mcmeta");
        if (var0 != null) {
            try (InputStream var1 = var0.get()) {
                T var2 = AbstractPackResources.getMetadataFromStream(param0, var1);
                if (var2 != null) {
                    return var2;
                }

                return this.metadata.get(param0);
            } catch (IOException var8) {
            }
        }

        return this.metadata.get(param0);
    }

    @Override
    public String packId() {
        return "vanilla";
    }

    @Override
    public boolean isBuiltin() {
        return true;
    }

    @Override
    public void close() {
    }

    public ResourceProvider asProvider() {
        return param0 -> Optional.ofNullable(this.getResource(PackType.CLIENT_RESOURCES, param0)).map(param0x -> new Resource(this, param0x));
    }
}

package net.minecraft.server.packs;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.Util;
import org.slf4j.Logger;

public class VanillaPackResourcesBuilder {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static Consumer<VanillaPackResourcesBuilder> developmentConfig = param0 -> {
    };
    private static final Map<PackType, Path> ROOT_DIR_BY_TYPE = Util.make(() -> {
        synchronized(VanillaPackResources.class) {
            Builder<PackType, Path> var0 = ImmutableMap.builder();

            for(PackType var1 : PackType.values()) {
                String var2 = "/" + var1.getDirectory() + "/.mcassetsroot";
                URL var3 = VanillaPackResources.class.getResource(var2);
                if (var3 == null) {
                    LOGGER.error("File {} does not exist in classpath", var2);
                } else {
                    try {
                        URI var4 = var3.toURI();
                        String var5 = var4.getScheme();
                        if (!"jar".equals(var5) && !"file".equals(var5)) {
                            LOGGER.warn("Assets URL '{}' uses unexpected schema", var4);
                        }

                        Path var6 = safeGetPath(var4);
                        var0.put(var1, var6.getParent());
                    } catch (Exception var12) {
                        LOGGER.error("Couldn't resolve path to vanilla assets", (Throwable)var12);
                    }
                }
            }

            return var0.build();
        }
    });
    private final Set<Path> rootPaths = new LinkedHashSet<>();
    private final Map<PackType, Set<Path>> pathsForType = new EnumMap<>(PackType.class);
    private BuiltInMetadata metadata = BuiltInMetadata.of();
    private final Set<String> namespaces = new HashSet<>();

    private static Path safeGetPath(URI param0) throws IOException {
        try {
            return Paths.get(param0);
        } catch (FileSystemNotFoundException var3) {
        } catch (Throwable var4) {
            LOGGER.warn("Unable to get path for: {}", param0, var4);
        }

        try {
            FileSystems.newFileSystem(param0, Collections.emptyMap());
        } catch (FileSystemAlreadyExistsException var2) {
        }

        return Paths.get(param0);
    }

    private boolean validateDirPath(Path param0) {
        if (!Files.exists(param0)) {
            return false;
        } else if (!Files.isDirectory(param0)) {
            throw new IllegalArgumentException("Path " + param0.toAbsolutePath() + " is not directory");
        } else {
            return true;
        }
    }

    private void pushRootPath(Path param0) {
        if (this.validateDirPath(param0)) {
            this.rootPaths.add(param0);
        }

    }

    private void pushPathForType(PackType param0, Path param1) {
        if (this.validateDirPath(param1)) {
            this.pathsForType.computeIfAbsent(param0, param0x -> new LinkedHashSet()).add(param1);
        }

    }

    public VanillaPackResourcesBuilder pushJarResources() {
        ROOT_DIR_BY_TYPE.forEach((param0, param1) -> {
            this.pushRootPath(param1.getParent());
            this.pushPathForType(param0, param1);
        });
        return this;
    }

    public VanillaPackResourcesBuilder pushClasspathResources(PackType param0, Class<?> param1) {
        Enumeration<URL> var0 = null;

        try {
            var0 = param1.getClassLoader().getResources(param0.getDirectory() + "/");
        } catch (IOException var8) {
        }

        while(var0 != null && var0.hasMoreElements()) {
            URL var1 = var0.nextElement();

            try {
                URI var2 = var1.toURI();
                if ("file".equals(var2.getScheme())) {
                    Path var3 = Paths.get(var2);
                    this.pushRootPath(var3.getParent());
                    this.pushPathForType(param0, var3);
                }
            } catch (Exception var7) {
                LOGGER.error("Failed to extract path from {}", var1, var7);
            }
        }

        return this;
    }

    public VanillaPackResourcesBuilder applyDevelopmentConfig() {
        developmentConfig.accept(this);
        return this;
    }

    public VanillaPackResourcesBuilder pushUniversalPath(Path param0) {
        this.pushRootPath(param0);

        for(PackType var0 : PackType.values()) {
            this.pushPathForType(var0, param0.resolve(var0.getDirectory()));
        }

        return this;
    }

    public VanillaPackResourcesBuilder pushAssetPath(PackType param0, Path param1) {
        this.pushRootPath(param1);
        this.pushPathForType(param0, param1);
        return this;
    }

    public VanillaPackResourcesBuilder setMetadata(BuiltInMetadata param0) {
        this.metadata = param0;
        return this;
    }

    public VanillaPackResourcesBuilder exposeNamespace(String... param0) {
        this.namespaces.addAll(Arrays.asList(param0));
        return this;
    }

    public VanillaPackResources build() {
        Map<PackType, List<Path>> var0 = new EnumMap<>(PackType.class);

        for(PackType var1 : PackType.values()) {
            List<Path> var2 = copyAndReverse(this.pathsForType.getOrDefault(var1, Set.of()));
            var0.put(var1, var2);
        }

        return new VanillaPackResources(this.metadata, Set.copyOf(this.namespaces), copyAndReverse(this.rootPaths), var0);
    }

    private static List<Path> copyAndReverse(Collection<Path> param0) {
        List<Path> var0 = new ArrayList<>(param0);
        Collections.reverse(var0);
        return List.copyOf(var0);
    }
}

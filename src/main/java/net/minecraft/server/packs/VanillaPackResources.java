package net.minecraft.server.packs;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableMap.Builder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VanillaPackResources implements PackResources, ResourceProvider {
    public static Path generatedDir;
    private static final Logger LOGGER = LogManager.getLogger();
    public static Class<?> clientObject;
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
    public final PackMetadataSection packMetadata;
    public final Set<String> namespaces;

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

    public VanillaPackResources(PackMetadataSection param0, String... param1) {
        this.packMetadata = param0;
        this.namespaces = ImmutableSet.copyOf(param1);
    }

    @Override
    public InputStream getRootResource(String param0) throws IOException {
        if (!param0.contains("/") && !param0.contains("\\")) {
            if (generatedDir != null) {
                Path var0 = generatedDir.resolve(param0);
                if (Files.exists(var0)) {
                    return Files.newInputStream(var0);
                }
            }

            return this.getResourceAsStream(param0);
        } else {
            throw new IllegalArgumentException("Root resources can only be filenames, not paths (no / allowed!)");
        }
    }

    @Override
    public InputStream getResource(PackType param0, ResourceLocation param1) throws IOException {
        InputStream var0 = this.getResourceAsStream(param0, param1);
        if (var0 != null) {
            return var0;
        } else {
            throw new FileNotFoundException(param1.getPath());
        }
    }

    @Override
    public Collection<ResourceLocation> getResources(PackType param0, String param1, String param2, int param3, Predicate<String> param4) {
        Set<ResourceLocation> var0 = Sets.newHashSet();
        if (generatedDir != null) {
            try {
                getResources(var0, param3, param1, generatedDir.resolve(param0.getDirectory()), param2, param4);
            } catch (IOException var13) {
            }

            if (param0 == PackType.CLIENT_RESOURCES) {
                Enumeration<URL> var1 = null;

                try {
                    var1 = clientObject.getClassLoader().getResources(param0.getDirectory() + "/");
                } catch (IOException var12) {
                }

                while(var1 != null && var1.hasMoreElements()) {
                    try {
                        URI var2 = var1.nextElement().toURI();
                        if ("file".equals(var2.getScheme())) {
                            getResources(var0, param3, param1, Paths.get(var2), param2, param4);
                        }
                    } catch (IOException | URISyntaxException var11) {
                    }
                }
            }
        }

        try {
            Path var3 = ROOT_DIR_BY_TYPE.get(param0);
            if (var3 != null) {
                getResources(var0, param3, param1, var3, param2, param4);
            } else {
                LOGGER.error("Can't access assets root for type: {}", param0);
            }
        } catch (NoSuchFileException | FileNotFoundException var9) {
        } catch (IOException var10) {
            LOGGER.error("Couldn't get a list of all vanilla resources", (Throwable)var10);
        }

        return var0;
    }

    private static void getResources(Collection<ResourceLocation> param0, int param1, String param2, Path param3, String param4, Predicate<String> param5) throws IOException {
        Path var0 = param3.resolve(param2);

        try (Stream<Path> var1 = Files.walk(var0.resolve(param4), param1)) {
            var1.filter(param1x -> !param1x.endsWith(".mcmeta") && Files.isRegularFile(param1x) && param5.test(param1x.getFileName().toString()))
                .map(param2x -> new ResourceLocation(param2, var0.relativize(param2x).toString().replaceAll("\\\\", "/")))
                .forEach(param0::add);
        }

    }

    @Nullable
    protected InputStream getResourceAsStream(PackType param0, ResourceLocation param1) {
        String var0 = createPath(param0, param1);
        if (generatedDir != null) {
            Path var1 = generatedDir.resolve(param0.getDirectory() + "/" + param1.getNamespace() + "/" + param1.getPath());
            if (Files.exists(var1)) {
                try {
                    return Files.newInputStream(var1);
                } catch (IOException var7) {
                }
            }
        }

        try {
            URL var2 = VanillaPackResources.class.getResource(var0);
            return isResourceUrlValid(var0, var2) ? var2.openStream() : null;
        } catch (IOException var6) {
            return VanillaPackResources.class.getResourceAsStream(var0);
        }
    }

    private static String createPath(PackType param0, ResourceLocation param1) {
        return "/" + param0.getDirectory() + "/" + param1.getNamespace() + "/" + param1.getPath();
    }

    private static boolean isResourceUrlValid(String param0, @Nullable URL param1) throws IOException {
        return param1 != null && (param1.getProtocol().equals("jar") || FolderPackResources.validatePath(new File(param1.getFile()), param0));
    }

    @Nullable
    protected InputStream getResourceAsStream(String param0) {
        return VanillaPackResources.class.getResourceAsStream("/" + param0);
    }

    @Override
    public boolean hasResource(PackType param0, ResourceLocation param1) {
        String var0 = createPath(param0, param1);
        if (generatedDir != null) {
            Path var1 = generatedDir.resolve(param0.getDirectory() + "/" + param1.getNamespace() + "/" + param1.getPath());
            if (Files.exists(var1)) {
                return true;
            }
        }

        try {
            URL var2 = VanillaPackResources.class.getResource(var0);
            return isResourceUrlValid(var0, var2);
        } catch (IOException var5) {
            return false;
        }
    }

    @Override
    public Set<String> getNamespaces(PackType param0) {
        return this.namespaces;
    }

    @Nullable
    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> param0) throws IOException {
        try (InputStream var0 = this.getRootResource("pack.mcmeta")) {
            if (var0 != null) {
                T var1 = AbstractPackResources.getMetadataFromStream(param0, var0);
                if (var1 != null) {
                    return var1;
                }
            }

            return (T)(param0 == PackMetadataSection.SERIALIZER ? this.packMetadata : null);
        } catch (FileNotFoundException | RuntimeException var7) {
            return (T)(param0 == PackMetadataSection.SERIALIZER ? this.packMetadata : null);
        }
    }

    @Override
    public String getName() {
        return "Default";
    }

    @Override
    public void close() {
    }

    @Override
    public Resource getResource(final ResourceLocation param0) throws IOException {
        return new Resource() {
            @Nullable
            InputStream inputStream;

            @Override
            public void close() throws IOException {
                if (this.inputStream != null) {
                    this.inputStream.close();
                }

            }

            @Override
            public ResourceLocation getLocation() {
                return param0;
            }

            @Override
            public InputStream getInputStream() {
                try {
                    this.inputStream = VanillaPackResources.this.getResource(PackType.CLIENT_RESOURCES, param0);
                } catch (IOException var2) {
                    throw new UncheckedIOException("Could not get client resource from vanilla pack", var2);
                }

                return this.inputStream;
            }

            @Override
            public boolean hasMetadata() {
                return false;
            }

            @Nullable
            @Override
            public <T> T getMetadata(MetadataSectionSerializer<T> param0x) {
                return null;
            }

            @Override
            public String getSourceName() {
                return param0.toString();
            }
        };
    }
}

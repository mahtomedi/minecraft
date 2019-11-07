package net.minecraft.server.packs;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VanillaPack implements Pack {
    public static Path generatedDir;
    private static final Logger LOGGER = LogManager.getLogger();
    public static Class<?> clientObject;
    private static final Map<PackType, FileSystem> JAR_FILESYSTEM_BY_TYPE = Util.make(Maps.newHashMap(), param0 -> {
        synchronized(VanillaPack.class) {
            for(PackType var0 : PackType.values()) {
                URL var1 = VanillaPack.class.getResource("/" + var0.getDirectory() + "/.mcassetsroot");

                try {
                    URI var2 = var1.toURI();
                    if ("jar".equals(var2.getScheme())) {
                        FileSystem var3;
                        try {
                            var3 = FileSystems.getFileSystem(var2);
                        } catch (FileSystemNotFoundException var11) {
                            var3 = FileSystems.newFileSystem(var2, Collections.emptyMap());
                        }

                        param0.put(var0, var3);
                    }
                } catch (IOException | URISyntaxException var12) {
                    LOGGER.error("Couldn't get a list of all vanilla resources", (Throwable)var12);
                }
            }

        }
    });
    public final Set<String> namespaces;

    public VanillaPack(String... param0) {
        this.namespaces = ImmutableSet.copyOf(param0);
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
            } catch (IOException var15) {
            }

            if (param0 == PackType.CLIENT_RESOURCES) {
                Enumeration<URL> var1 = null;

                try {
                    var1 = clientObject.getClassLoader().getResources(param0.getDirectory() + "/");
                } catch (IOException var14) {
                }

                while(var1 != null && var1.hasMoreElements()) {
                    try {
                        URI var2 = var1.nextElement().toURI();
                        if ("file".equals(var2.getScheme())) {
                            getResources(var0, param3, param1, Paths.get(var2), param2, param4);
                        }
                    } catch (IOException | URISyntaxException var13) {
                    }
                }
            }
        }

        try {
            URL var3 = VanillaPack.class.getResource("/" + param0.getDirectory() + "/.mcassetsroot");
            if (var3 == null) {
                LOGGER.error("Couldn't find .mcassetsroot, cannot load vanilla resources");
                return var0;
            }

            URI var4 = var3.toURI();
            if ("file".equals(var4.getScheme())) {
                URL var5 = new URL(var3.toString().substring(0, var3.toString().length() - ".mcassetsroot".length()));
                Path var6 = Paths.get(var5.toURI());
                getResources(var0, param3, param1, var6, param2, param4);
            } else if ("jar".equals(var4.getScheme())) {
                Path var7 = JAR_FILESYSTEM_BY_TYPE.get(param0).getPath("/" + param0.getDirectory());
                getResources(var0, param3, "minecraft", var7, param2, param4);
            } else {
                LOGGER.error("Unsupported scheme {} trying to list vanilla resources (NYI?)", var4);
            }
        } catch (NoSuchFileException | FileNotFoundException var11) {
        } catch (IOException | URISyntaxException var12) {
            LOGGER.error("Couldn't get a list of all vanilla resources", (Throwable)var12);
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
            URL var2 = VanillaPack.class.getResource(var0);
            return isResourceUrlValid(var0, var2) ? var2.openStream() : null;
        } catch (IOException var6) {
            return VanillaPack.class.getResourceAsStream(var0);
        }
    }

    private static String createPath(PackType param0, ResourceLocation param1) {
        return "/" + param0.getDirectory() + "/" + param1.getNamespace() + "/" + param1.getPath();
    }

    private static boolean isResourceUrlValid(String param0, @Nullable URL param1) throws IOException {
        return param1 != null && (param1.getProtocol().equals("jar") || FolderResourcePack.validatePath(new File(param1.getFile()), param0));
    }

    @Nullable
    protected InputStream getResourceAsStream(String param0) {
        return VanillaPack.class.getResourceAsStream("/" + param0);
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
            URL var2 = VanillaPack.class.getResource(var0);
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
            return AbstractResourcePack.getMetadataFromStream(param0, var0);
        } catch (FileNotFoundException | RuntimeException var16) {
            return null;
        }
    }

    @Override
    public String getName() {
        return "Default";
    }

    @Override
    public void close() {
    }
}

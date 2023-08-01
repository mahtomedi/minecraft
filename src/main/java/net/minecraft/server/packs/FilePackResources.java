package net.minecraft.server.packs;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.IoSupplier;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class FilePackResources extends AbstractPackResources {
    static final Logger LOGGER = LogUtils.getLogger();
    private final FilePackResources.SharedZipFileAccess zipFileAccess;
    private final String prefix;

    FilePackResources(String param0, FilePackResources.SharedZipFileAccess param1, boolean param2, String param3) {
        super(param0, param2);
        this.zipFileAccess = param1;
        this.prefix = param3;
    }

    private static String getPathFromLocation(PackType param0, ResourceLocation param1) {
        return String.format(Locale.ROOT, "%s/%s/%s", param0.getDirectory(), param1.getNamespace(), param1.getPath());
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String... param0) {
        return this.getResource(String.join("/", param0));
    }

    @Override
    public IoSupplier<InputStream> getResource(PackType param0, ResourceLocation param1) {
        return this.getResource(getPathFromLocation(param0, param1));
    }

    private String addPrefix(String param0) {
        return this.prefix.isEmpty() ? param0 : this.prefix + "/" + param0;
    }

    @Nullable
    private IoSupplier<InputStream> getResource(String param0) {
        ZipFile var0 = this.zipFileAccess.getOrCreateZipFile();
        if (var0 == null) {
            return null;
        } else {
            ZipEntry var1 = var0.getEntry(this.addPrefix(param0));
            return var1 == null ? null : IoSupplier.create(var0, var1);
        }
    }

    @Override
    public Set<String> getNamespaces(PackType param0) {
        ZipFile var0 = this.zipFileAccess.getOrCreateZipFile();
        if (var0 == null) {
            return Set.of();
        } else {
            Enumeration<? extends ZipEntry> var1 = var0.entries();
            Set<String> var2 = Sets.newHashSet();
            String var3 = this.addPrefix(param0.getDirectory() + "/");

            while(var1.hasMoreElements()) {
                ZipEntry var4 = var1.nextElement();
                String var5 = var4.getName();
                String var6 = extractNamespace(var3, var5);
                if (!var6.isEmpty()) {
                    if (ResourceLocation.isValidNamespace(var6)) {
                        var2.add(var6);
                    } else {
                        LOGGER.warn("Non [a-z0-9_.-] character in namespace {} in pack {}, ignoring", var6, this.zipFileAccess.file);
                    }
                }
            }

            return var2;
        }
    }

    @VisibleForTesting
    public static String extractNamespace(String param0, String param1) {
        if (!param1.startsWith(param0)) {
            return "";
        } else {
            int var0 = param0.length();
            int var1 = param1.indexOf(47, var0);
            return var1 == -1 ? param1.substring(var0) : param1.substring(var0, var1);
        }
    }

    @Override
    public void close() {
        this.zipFileAccess.close();
    }

    @Override
    public void listResources(PackType param0, String param1, String param2, PackResources.ResourceOutput param3) {
        ZipFile var0 = this.zipFileAccess.getOrCreateZipFile();
        if (var0 != null) {
            Enumeration<? extends ZipEntry> var1 = var0.entries();
            String var2 = this.addPrefix(param0.getDirectory() + "/" + param1 + "/");
            String var3 = var2 + param2 + "/";

            while(var1.hasMoreElements()) {
                ZipEntry var4 = var1.nextElement();
                if (!var4.isDirectory()) {
                    String var5 = var4.getName();
                    if (var5.startsWith(var3)) {
                        String var6 = var5.substring(var2.length());
                        ResourceLocation var7 = ResourceLocation.tryBuild(param1, var6);
                        if (var7 != null) {
                            param3.accept(var7, IoSupplier.create(var0, var4));
                        } else {
                            LOGGER.warn("Invalid path in datapack: {}:{}, ignoring", param1, var6);
                        }
                    }
                }
            }

        }
    }

    public static class FileResourcesSupplier implements Pack.ResourcesSupplier {
        private final File content;
        private final boolean isBuiltin;

        public FileResourcesSupplier(Path param0, boolean param1) {
            this(param0.toFile(), param1);
        }

        public FileResourcesSupplier(File param0, boolean param1) {
            this.isBuiltin = param1;
            this.content = param0;
        }

        @Override
        public PackResources openPrimary(String param0) {
            FilePackResources.SharedZipFileAccess var0 = new FilePackResources.SharedZipFileAccess(this.content);
            return new FilePackResources(param0, var0, this.isBuiltin, "");
        }

        @Override
        public PackResources openFull(String param0, Pack.Info param1) {
            FilePackResources.SharedZipFileAccess var0 = new FilePackResources.SharedZipFileAccess(this.content);
            PackResources var1 = new FilePackResources(param0, var0, this.isBuiltin, "");
            List<String> var2 = param1.overlays();
            if (var2.isEmpty()) {
                return var1;
            } else {
                List<PackResources> var3 = new ArrayList<>(var2.size());

                for(String var4 : var2) {
                    var3.add(new FilePackResources(param0, var0, this.isBuiltin, var4));
                }

                return new CompositePackResources(var1, var3);
            }
        }
    }

    static class SharedZipFileAccess implements AutoCloseable {
        final File file;
        @Nullable
        private ZipFile zipFile;
        private boolean failedToLoad;

        SharedZipFileAccess(File param0) {
            this.file = param0;
        }

        @Nullable
        ZipFile getOrCreateZipFile() {
            if (this.failedToLoad) {
                return null;
            } else {
                if (this.zipFile == null) {
                    try {
                        this.zipFile = new ZipFile(this.file);
                    } catch (IOException var2) {
                        FilePackResources.LOGGER.error("Failed to open pack {}", this.file, var2);
                        this.failedToLoad = true;
                        return null;
                    }
                }

                return this.zipFile;
            }
        }

        @Override
        public void close() {
            if (this.zipFile != null) {
                IOUtils.closeQuietly((Closeable)this.zipFile);
                this.zipFile = null;
            }

        }

        @Override
        protected void finalize() throws Throwable {
            this.close();
            super.finalize();
        }
    }
}

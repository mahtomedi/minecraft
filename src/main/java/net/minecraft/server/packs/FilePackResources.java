package net.minecraft.server.packs;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.IoSupplier;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class FilePackResources extends AbstractPackResources {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Splitter SPLITTER = Splitter.on('/').omitEmptyStrings().limit(3);
    private final File file;
    @Nullable
    private ZipFile zipFile;
    private boolean failedToLoad;

    public FilePackResources(String param0, File param1) {
        super(param0);
        this.file = param1;
    }

    @Nullable
    private ZipFile getOrCreateZipFile() {
        if (this.failedToLoad) {
            return null;
        } else {
            if (this.zipFile == null) {
                try {
                    this.zipFile = new ZipFile(this.file);
                } catch (IOException var2) {
                    LOGGER.error("Failed to open pack {}", this.file, var2);
                    this.failedToLoad = true;
                    return null;
                }
            }

            return this.zipFile;
        }
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

    @Nullable
    private IoSupplier<InputStream> getResource(String param0) {
        ZipFile var0 = this.getOrCreateZipFile();
        if (var0 == null) {
            return null;
        } else {
            ZipEntry var1 = var0.getEntry(param0);
            return var1 == null ? null : IoSupplier.create(var0, var1);
        }
    }

    @Override
    public Set<String> getNamespaces(PackType param0) {
        ZipFile var0 = this.getOrCreateZipFile();
        if (var0 == null) {
            return Set.of();
        } else {
            Enumeration<? extends ZipEntry> var1 = var0.entries();
            Set<String> var2 = Sets.newHashSet();

            while(var1.hasMoreElements()) {
                ZipEntry var3 = var1.nextElement();
                String var4 = var3.getName();
                if (var4.startsWith(param0.getDirectory() + "/")) {
                    List<String> var5 = Lists.newArrayList(SPLITTER.split(var4));
                    if (var5.size() > 1) {
                        String var6 = var5.get(1);
                        if (var6.equals(var6.toLowerCase(Locale.ROOT))) {
                            var2.add(var6);
                        } else {
                            LOGGER.warn("Ignored non-lowercase namespace: {} in {}", var6, this.file);
                        }
                    }
                }
            }

            return var2;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }

    @Override
    public void close() {
        if (this.zipFile != null) {
            IOUtils.closeQuietly((Closeable)this.zipFile);
            this.zipFile = null;
        }

    }

    @Override
    public void listResources(PackType param0, String param1, String param2, PackResources.ResourceOutput param3) {
        ZipFile var0 = this.getOrCreateZipFile();
        if (var0 != null) {
            Enumeration<? extends ZipEntry> var1 = var0.entries();
            String var2 = param0.getDirectory() + "/" + param1 + "/";
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
}

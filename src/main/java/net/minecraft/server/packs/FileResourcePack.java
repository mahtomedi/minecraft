package net.minecraft.server.packs;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.IOUtils;

public class FileResourcePack extends AbstractResourcePack {
    public static final Splitter SPLITTER = Splitter.on('/').omitEmptyStrings().limit(3);
    private ZipFile zipFile;

    public FileResourcePack(File param0) {
        super(param0);
    }

    private ZipFile getOrCreateZipFile() throws IOException {
        if (this.zipFile == null) {
            this.zipFile = new ZipFile(this.file);
        }

        return this.zipFile;
    }

    @Override
    protected InputStream getResource(String param0) throws IOException {
        ZipFile var0 = this.getOrCreateZipFile();
        ZipEntry var1 = var0.getEntry(param0);
        if (var1 == null) {
            throw new ResourcePackFileNotFoundException(this.file, param0);
        } else {
            return var0.getInputStream(var1);
        }
    }

    @Override
    public boolean hasResource(String param0) {
        try {
            return this.getOrCreateZipFile().getEntry(param0) != null;
        } catch (IOException var3) {
            return false;
        }
    }

    @Override
    public Set<String> getNamespaces(PackType param0) {
        ZipFile var0;
        try {
            var0 = this.getOrCreateZipFile();
        } catch (IOException var9) {
            return Collections.emptySet();
        }

        Enumeration<? extends ZipEntry> var3 = var0.entries();
        Set<String> var4 = Sets.newHashSet();

        while(var3.hasMoreElements()) {
            ZipEntry var5 = var3.nextElement();
            String var6 = var5.getName();
            if (var6.startsWith(param0.getDirectory() + "/")) {
                List<String> var7 = Lists.newArrayList(SPLITTER.split(var6));
                if (var7.size() > 1) {
                    String var8 = var7.get(1);
                    if (var8.equals(var8.toLowerCase(Locale.ROOT))) {
                        var4.add(var8);
                    } else {
                        this.logWarning(var8);
                    }
                }
            }
        }

        return var4;
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
    public Collection<ResourceLocation> getResources(PackType param0, String param1, String param2, int param3, Predicate<String> param4) {
        ZipFile var0;
        try {
            var0 = this.getOrCreateZipFile();
        } catch (IOException var15) {
            return Collections.emptySet();
        }

        Enumeration<? extends ZipEntry> var3 = var0.entries();
        List<ResourceLocation> var4 = Lists.newArrayList();
        String var5 = param0.getDirectory() + "/" + param1 + "/";
        String var6 = var5 + param2 + "/";

        while(var3.hasMoreElements()) {
            ZipEntry var7 = var3.nextElement();
            if (!var7.isDirectory()) {
                String var8 = var7.getName();
                if (!var8.endsWith(".mcmeta") && var8.startsWith(var6)) {
                    String var9 = var8.substring(var5.length());
                    String[] var10 = var9.split("/");
                    if (var10.length >= param3 + 1 && param4.test(var10[var10.length - 1])) {
                        var4.add(new ResourceLocation(param1, var9));
                    }
                }
            }
        }

        return var4;
    }
}

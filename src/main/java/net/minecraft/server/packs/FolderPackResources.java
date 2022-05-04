package net.minecraft.server.packs;

import com.google.common.base.CharMatcher;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.ResourceLocationException;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.slf4j.Logger;

public class FolderPackResources extends AbstractPackResources {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final boolean ON_WINDOWS = Util.getPlatform() == Util.OS.WINDOWS;
    private static final CharMatcher BACKSLASH_MATCHER = CharMatcher.is('\\');

    public FolderPackResources(File param0) {
        super(param0);
    }

    public static boolean validatePath(File param0, String param1) throws IOException {
        String var0 = param0.getCanonicalPath();
        if (ON_WINDOWS) {
            var0 = BACKSLASH_MATCHER.replaceFrom(var0, '/');
        }

        return var0.endsWith(param1);
    }

    @Override
    protected InputStream getResource(String param0) throws IOException {
        File var0 = this.getFile(param0);
        if (var0 == null) {
            throw new ResourcePackFileNotFoundException(this.file, param0);
        } else {
            return new FileInputStream(var0);
        }
    }

    @Override
    protected boolean hasResource(String param0) {
        return this.getFile(param0) != null;
    }

    @Nullable
    private File getFile(String param0) {
        try {
            File var0 = new File(this.file, param0);
            if (var0.isFile() && validatePath(var0, param0)) {
                return var0;
            }
        } catch (IOException var3) {
        }

        return null;
    }

    @Override
    public Set<String> getNamespaces(PackType param0) {
        Set<String> var0 = Sets.newHashSet();
        File var1 = new File(this.file, param0.getDirectory());
        File[] var2 = var1.listFiles((FileFilter)DirectoryFileFilter.DIRECTORY);
        if (var2 != null) {
            for(File var3 : var2) {
                String var4 = getRelativePath(var1, var3);
                if (var4.equals(var4.toLowerCase(Locale.ROOT))) {
                    var0.add(var4.substring(0, var4.length() - 1));
                } else {
                    this.logWarning(var4);
                }
            }
        }

        return var0;
    }

    @Override
    public void close() {
    }

    @Override
    public Collection<ResourceLocation> getResources(PackType param0, String param1, String param2, Predicate<ResourceLocation> param3) {
        File var0 = new File(this.file, param0.getDirectory());
        List<ResourceLocation> var1 = Lists.newArrayList();
        this.listResources(new File(new File(var0, param1), param2), param1, var1, param2 + "/", param3);
        return var1;
    }

    private void listResources(File param0, String param1, List<ResourceLocation> param2, String param3, Predicate<ResourceLocation> param4) {
        File[] var0 = param0.listFiles();
        if (var0 != null) {
            for(File var1 : var0) {
                if (var1.isDirectory()) {
                    this.listResources(var1, param1, param2, param3 + var1.getName() + "/", param4);
                } else if (!var1.getName().endsWith(".mcmeta")) {
                    try {
                        String var2 = param3 + var1.getName();
                        ResourceLocation var3 = ResourceLocation.tryBuild(param1, var2);
                        if (var3 == null) {
                            LOGGER.warn("Invalid path in datapack: {}:{}, ignoring", param1, var2);
                        } else if (param4.test(var3)) {
                            param2.add(var3);
                        }
                    } catch (ResourceLocationException var13) {
                        LOGGER.error(var13.getMessage());
                    }
                }
            }
        }

    }
}

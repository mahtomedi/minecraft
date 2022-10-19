package net.minecraft.server.packs;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.IoSupplier;
import org.slf4j.Logger;

public class PathPackResources extends AbstractPackResources {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Joiner PATH_JOINER = Joiner.on("/");
    private final Path root;

    public PathPackResources(String param0, Path param1) {
        super(param0);
        this.root = param1;
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String... param0) {
        FileUtil.validatePath(param0);
        Path var0 = FileUtil.resolvePath(this.root, List.of(param0));
        return Files.exists(var0) ? IoSupplier.create(var0) : null;
    }

    public static boolean validatePath(Path param0) {
        return true;
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getResource(PackType param0, ResourceLocation param1) {
        Path var0 = this.root.resolve(param0.getDirectory()).resolve(param1.getNamespace());
        return getResource(param1, var0);
    }

    public static IoSupplier<InputStream> getResource(ResourceLocation param0, Path param1) {
        return FileUtil.decomposePath(param0.getPath()).get().map(param1x -> {
            Path var0x = FileUtil.resolvePath(param1, param1x);
            return returnFileIfExists(var0x);
        }, param1x -> {
            LOGGER.error("Invalid path {}: {}", param0, param1x.message());
            return null;
        });
    }

    @Nullable
    private static IoSupplier<InputStream> returnFileIfExists(Path param0) {
        return Files.exists(param0) && validatePath(param0) ? IoSupplier.create(param0) : null;
    }

    @Override
    public void listResources(PackType param0, String param1, String param2, PackResources.ResourceOutput param3) {
        FileUtil.decomposePath(param2).get().ifLeft(param3x -> {
            Path var0 = this.root.resolve(param0.getDirectory()).resolve(param1);
            listPath(param1, var0, param3x, param3);
        }).ifRight(param1x -> LOGGER.error("Invalid path {}: {}", param2, param1x.message()));
    }

    public static void listPath(String param0, Path param1, List<String> param2, PackResources.ResourceOutput param3) {
        Path var0 = FileUtil.resolvePath(param1, param2);

        try (Stream<Path> var1 = Files.find(var0, Integer.MAX_VALUE, (param0x, param1x) -> param1x.isRegularFile())) {
            var1.forEach(param3x -> {
                String var0x = PATH_JOINER.join(param1.relativize(param3x));
                ResourceLocation var1x = ResourceLocation.tryBuild(param0, var0x);
                if (var1x == null) {
                    Util.logAndPauseIfInIde(String.format(Locale.ROOT, "Invalid path in pack: %s:%s, ignoring", param0, var0x));
                } else {
                    param3.accept(var1x, IoSupplier.create(param3x));
                }

            });
        } catch (NoSuchFileException var10) {
        } catch (IOException var11) {
            LOGGER.error("Failed to list path {}", var0, var11);
        }

    }

    @Override
    public Set<String> getNamespaces(PackType param0) {
        Set<String> var0 = Sets.newHashSet();
        Path var1 = this.root.resolve(param0.getDirectory());

        try (DirectoryStream<Path> var2 = Files.newDirectoryStream(var1)) {
            for(Path var3 : var2) {
                String var4 = var3.getFileName().toString();
                if (var4.equals(var4.toLowerCase(Locale.ROOT))) {
                    var0.add(var4);
                } else {
                    LOGGER.warn("Ignored non-lowercase namespace: {} in {}", var4, this.root);
                }
            }
        } catch (IOException var10) {
            LOGGER.error("Failed to list path {}", var1, var10);
        }

        return var0;
    }

    @Override
    public void close() {
    }
}

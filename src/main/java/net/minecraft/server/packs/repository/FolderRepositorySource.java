package net.minecraft.server.packs.repository;

import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.linkfs.LinkFileSystem;
import org.slf4j.Logger;

public class FolderRepositorySource implements RepositorySource {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Path folder;
    private final PackType packType;
    private final PackSource packSource;

    public FolderRepositorySource(Path param0, PackType param1, PackSource param2) {
        this.folder = param0;
        this.packType = param1;
        this.packSource = param2;
    }

    private static String nameFromPath(Path param0) {
        return param0.getFileName().toString();
    }

    @Override
    public void loadPacks(Consumer<Pack> param0) {
        try {
            Files.createDirectories(this.folder);
            discoverPacks(
                this.folder,
                (param1, param2) -> {
                    String var0x = nameFromPath(param1);
                    Pack var1x = Pack.readMetaAndCreate(
                        "file/" + var0x, Component.literal(var0x), false, param2, this.packType, Pack.Position.TOP, this.packSource
                    );
                    if (var1x != null) {
                        param0.accept(var1x);
                    }
    
                }
            );
        } catch (IOException var3) {
            LOGGER.warn("Failed to list packs in {}", this.folder, var3);
        }

    }

    public static void discoverPacks(Path param0, BiConsumer<Path, Pack.ResourcesSupplier> param1) throws IOException {
        try (DirectoryStream<Path> var0 = Files.newDirectoryStream(param0)) {
            for(Path var1 : var0) {
                Pack.ResourcesSupplier var2 = detectPackResources(var1);
                if (var2 != null) {
                    param1.accept(var1, var2);
                }
            }
        }

    }

    @Nullable
    public static Pack.ResourcesSupplier detectPackResources(Path param0) {
        BasicFileAttributes var0;
        try {
            var0 = Files.readAttributes(param0, BasicFileAttributes.class);
        } catch (NoSuchFileException var41) {
            return null;
        } catch (IOException var51) {
            LOGGER.warn("Failed to read properties of '{}', ignoring", param0, var51);
            return null;
        }

        if (var0.isDirectory() && Files.isRegularFile(param0.resolve("pack.mcmeta"))) {
            return param1 -> new PathPackResources(param1, param0);
        } else {
            if (var0.isRegularFile() && param0.getFileName().toString().endsWith(".zip")) {
                FileSystem var4 = param0.getFileSystem();
                if (var4 == FileSystems.getDefault() || var4 instanceof LinkFileSystem) {
                    File var5 = param0.toFile();
                    return param1 -> new FilePackResources(param1, var5);
                }
            }

            LOGGER.info("Found non-pack entry '{}', ignoring", param0);
            return null;
        }
    }
}

package net.minecraft.server.packs.repository;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.linkfs.LinkFileSystem;
import net.minecraft.world.level.validation.ContentValidationException;
import net.minecraft.world.level.validation.DirectoryValidator;
import net.minecraft.world.level.validation.ForbiddenSymlinkInfo;
import org.slf4j.Logger;

public class FolderRepositorySource implements RepositorySource {
    static final Logger LOGGER = LogUtils.getLogger();
    private final Path folder;
    private final PackType packType;
    private final PackSource packSource;
    private final DirectoryValidator validator;

    public FolderRepositorySource(Path param0, PackType param1, PackSource param2, DirectoryValidator param3) {
        this.folder = param0;
        this.packType = param1;
        this.packSource = param2;
        this.validator = param3;
    }

    private static String nameFromPath(Path param0) {
        return param0.getFileName().toString();
    }

    @Override
    public void loadPacks(Consumer<Pack> param0) {
        try {
            FileUtil.createDirectoriesSafe(this.folder);
            discoverPacks(
                this.folder,
                this.validator,
                false,
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

    public static void discoverPacks(Path param0, DirectoryValidator param1, boolean param2, BiConsumer<Path, Pack.ResourcesSupplier> param3) throws IOException {
        FolderRepositorySource.FolderPackDetector var0 = new FolderRepositorySource.FolderPackDetector(param1, param2);

        try (DirectoryStream<Path> var1 = Files.newDirectoryStream(param0)) {
            for(Path var2 : var1) {
                try {
                    List<ForbiddenSymlinkInfo> var3 = new ArrayList<>();
                    Pack.ResourcesSupplier var4 = var0.detectPackResources(var2, var3);
                    if (!var3.isEmpty()) {
                        LOGGER.warn("Ignoring potential pack entry: {}", ContentValidationException.getMessage(var2, var3));
                    } else if (var4 != null) {
                        param3.accept(var2, var4);
                    } else {
                        LOGGER.info("Found non-pack entry '{}', ignoring", var2);
                    }
                } catch (IOException var11) {
                    LOGGER.warn("Failed to read properties of '{}', ignoring", var2, var11);
                }
            }
        }

    }

    static class FolderPackDetector extends PackDetector<Pack.ResourcesSupplier> {
        private final boolean isBuiltin;

        protected FolderPackDetector(DirectoryValidator param0, boolean param1) {
            super(param0);
            this.isBuiltin = param1;
        }

        @Nullable
        protected Pack.ResourcesSupplier createZipPack(Path param0) {
            FileSystem var0 = param0.getFileSystem();
            if (var0 != FileSystems.getDefault() && !(var0 instanceof LinkFileSystem)) {
                FolderRepositorySource.LOGGER.info("Can't open pack archive at {}", param0);
                return null;
            } else {
                return new FilePackResources.FileResourcesSupplier(param0, this.isBuiltin);
            }
        }

        protected Pack.ResourcesSupplier createDirectoryPack(Path param0) {
            return new PathPackResources.PathResourcesSupplier(param0, this.isBuiltin);
        }
    }
}

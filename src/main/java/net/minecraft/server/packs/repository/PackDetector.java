package net.minecraft.server.packs.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.world.level.validation.DirectoryValidator;
import net.minecraft.world.level.validation.ForbiddenSymlinkInfo;

public abstract class PackDetector<T> {
    private final DirectoryValidator validator;

    protected PackDetector(DirectoryValidator param0) {
        this.validator = param0;
    }

    @Nullable
    public T detectPackResources(Path param0, List<ForbiddenSymlinkInfo> param1) throws IOException {
        Path var0 = param0;

        BasicFileAttributes var1;
        try {
            var1 = Files.readAttributes(param0, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        } catch (NoSuchFileException var6) {
            return null;
        }

        if (var1.isSymbolicLink()) {
            this.validator.validateSymlink(param0, param1);
            if (!param1.isEmpty()) {
                return null;
            }

            var0 = Files.readSymbolicLink(param0);
            var1 = Files.readAttributes(var0, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        }

        if (var1.isDirectory()) {
            this.validator.validateKnownDirectory(var0, param1);
            if (!param1.isEmpty()) {
                return null;
            } else {
                return !Files.isRegularFile(var0.resolve("pack.mcmeta")) ? null : this.createDirectoryPack(var0);
            }
        } else {
            return var1.isRegularFile() && var0.getFileName().toString().endsWith(".zip") ? this.createZipPack(var0) : null;
        }
    }

    @Nullable
    protected abstract T createZipPack(Path var1) throws IOException;

    @Nullable
    protected abstract T createDirectoryPack(Path var1) throws IOException;
}

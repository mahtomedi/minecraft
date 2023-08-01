package net.minecraft.world.level.validation;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class DirectoryValidator {
    private final PathMatcher symlinkTargetAllowList;

    public DirectoryValidator(PathMatcher param0) {
        this.symlinkTargetAllowList = param0;
    }

    public void validateSymlink(Path param0, List<ForbiddenSymlinkInfo> param1) throws IOException {
        Path var0 = Files.readSymbolicLink(param0);
        if (!this.symlinkTargetAllowList.matches(var0)) {
            param1.add(new ForbiddenSymlinkInfo(param0, var0));
        }

    }

    public List<ForbiddenSymlinkInfo> validateSymlink(Path param0) throws IOException {
        List<ForbiddenSymlinkInfo> var0 = new ArrayList<>();
        this.validateSymlink(param0, var0);
        return var0;
    }

    public List<ForbiddenSymlinkInfo> validateDirectory(Path param0, boolean param1) throws IOException {
        List<ForbiddenSymlinkInfo> var0 = new ArrayList<>();

        BasicFileAttributes var1;
        try {
            var1 = Files.readAttributes(param0, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        } catch (NoSuchFileException var6) {
            return var0;
        }

        if (!var1.isRegularFile() && !var1.isOther()) {
            if (var1.isSymbolicLink()) {
                if (!param1) {
                    this.validateSymlink(param0, var0);
                    return var0;
                }

                param0 = Files.readSymbolicLink(param0);
            }

            this.validateKnownDirectory(param0, var0);
            return var0;
        } else {
            throw new IOException("Path " + param0 + " is not a directory");
        }
    }

    public void validateKnownDirectory(Path param0, final List<ForbiddenSymlinkInfo> param1) throws IOException {
        Files.walkFileTree(param0, new SimpleFileVisitor<Path>() {
            private void validateSymlink(Path param0, BasicFileAttributes param1x) throws IOException {
                if (param1.isSymbolicLink()) {
                    DirectoryValidator.this.validateSymlink(param0, param1);
                }

            }

            public FileVisitResult preVisitDirectory(Path param0, BasicFileAttributes param1x) throws IOException {
                this.validateSymlink(param0, param1);
                return super.preVisitDirectory(param0, param1);
            }

            public FileVisitResult visitFile(Path param0, BasicFileAttributes param1x) throws IOException {
                this.validateSymlink(param0, param1);
                return super.visitFile(param0, param1);
            }
        });
    }
}

package net.minecraft.util;

import com.google.common.base.Charsets;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DirectoryLock implements AutoCloseable {
    private final FileChannel lockFile;
    private final FileLock lock;
    private static final ByteBuffer DUMMY;

    public static DirectoryLock create(Path param0) throws IOException {
        Path var0 = param0.resolve("session.lock");
        if (!Files.isDirectory(param0)) {
            Files.createDirectories(param0);
        }

        FileChannel var1 = FileChannel.open(var0, StandardOpenOption.CREATE, StandardOpenOption.WRITE);

        try {
            var1.write(DUMMY.duplicate());
            var1.force(true);
            FileLock var2 = var1.tryLock();
            if (var2 == null) {
                throw DirectoryLock.LockException.alreadyLocked(var0);
            } else {
                return new DirectoryLock(var1, var2);
            }
        } catch (IOException var6) {
            try {
                var1.close();
            } catch (IOException var5) {
                var6.addSuppressed(var5);
            }

            throw var6;
        }
    }

    private DirectoryLock(FileChannel param0, FileLock param1) {
        this.lockFile = param0;
        this.lock = param1;
    }

    @Override
    public void close() throws IOException {
        try {
            if (this.lock.isValid()) {
                this.lock.release();
            }
        } finally {
            if (this.lockFile.isOpen()) {
                this.lockFile.close();
            }

        }

    }

    public boolean isValid() {
        return this.lock.isValid();
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean isLocked(Path param0) throws IOException {
        Path var0 = param0.resolve("session.lock");

        try (
            FileChannel var1 = FileChannel.open(var0, StandardOpenOption.WRITE);
            FileLock var2 = var1.tryLock();
        ) {
            return var2 == null;
        } catch (AccessDeniedException var37) {
            return true;
        } catch (NoSuchFileException var38) {
            return false;
        }
    }

    static {
        byte[] var0 = "\u2603".getBytes(Charsets.UTF_8);
        DUMMY = ByteBuffer.allocateDirect(var0.length);
        DUMMY.put(var0);
        ((Buffer)DUMMY).flip();
    }

    public static class LockException extends IOException {
        private LockException(Path param0, String param1) {
            super(param0.toAbsolutePath() + ": " + param1);
        }

        public static DirectoryLock.LockException alreadyLocked(Path param0) {
            return new DirectoryLock.LockException(param0, "already locked (possibly by other Minecraft instance?)");
        }
    }
}

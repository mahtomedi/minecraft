package net.minecraft.util;

import com.google.common.base.Charsets;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.AccessDeniedException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import net.minecraft.FileUtil;

public class DirectoryLock implements AutoCloseable {
    public static final String LOCK_FILE = "session.lock";
    private final FileChannel lockFile;
    private final FileLock lock;
    private static final ByteBuffer DUMMY;

    public static DirectoryLock create(Path param0) throws IOException {
        Path var0 = param0.resolve("session.lock");
        FileUtil.createDirectoriesSafe(param0);

        try (FileChannel var1 = FileChannel.open(var0, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            var1.write(DUMMY.duplicate());
            var1.force(true);
            FileLock var2 = var1.tryLock();
            if (var2 == null) {
                throw DirectoryLock.LockException.alreadyLocked(var0);
            } else {
                return new DirectoryLock(var1, var2);
            }
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

    public static boolean isLocked(Path param0) throws IOException {
        Path var0 = param0.resolve("session.lock");

        try {
            boolean var41;
            try (
                FileChannel var1 = FileChannel.open(var0, StandardOpenOption.WRITE);
                FileLock var2 = var1.tryLock();
            ) {
                var41 = var2 == null;
            }

            return var41;
        } catch (AccessDeniedException var10) {
            return true;
        } catch (NoSuchFileException var11) {
            return false;
        }
    }

    static {
        byte[] var0 = "\u2603".getBytes(Charsets.UTF_8);
        DUMMY = ByteBuffer.allocateDirect(var0.length);
        DUMMY.put(var0);
        DUMMY.flip();
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

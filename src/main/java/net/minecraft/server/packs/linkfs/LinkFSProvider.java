package net.minecraft.server.packs.linkfs;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessDeniedException;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.ReadOnlyFileSystemException;
import java.nio.file.StandardOpenOption;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

class LinkFSProvider extends FileSystemProvider {
    public static final String SCHEME = "x-mc-link";

    @Override
    public String getScheme() {
        return "x-mc-link";
    }

    @Override
    public FileSystem newFileSystem(URI param0, Map<String, ?> param1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileSystem getFileSystem(URI param0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Path getPath(URI param0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SeekableByteChannel newByteChannel(Path param0, Set<? extends OpenOption> param1, FileAttribute<?>... param2) throws IOException {
        if (!param1.contains(StandardOpenOption.CREATE_NEW)
            && !param1.contains(StandardOpenOption.CREATE)
            && !param1.contains(StandardOpenOption.APPEND)
            && !param1.contains(StandardOpenOption.WRITE)) {
            Path var0 = toLinkPath(param0).toAbsolutePath().getTargetPath();
            if (var0 == null) {
                throw new NoSuchFileException(param0.toString());
            } else {
                return Files.newByteChannel(var0, param1, param2);
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path param0, final Filter<? super Path> param1) throws IOException {
        final PathContents.DirectoryContents var0 = toLinkPath(param0).toAbsolutePath().getDirectoryContents();
        if (var0 == null) {
            throw new NotDirectoryException(param0.toString());
        } else {
            return new DirectoryStream<Path>() {
                @Override
                public Iterator<Path> iterator() {
                    return var0.children().values().stream().filter(param1xx -> {
                        try {
                            return param1.accept(param1xx);
                        } catch (IOException var3) {
                            throw new DirectoryIteratorException(var3);
                        }
                    }).map(param0 -> param0).iterator();
                }

                @Override
                public void close() {
                }
            };
        }
    }

    @Override
    public void createDirectory(Path param0, FileAttribute<?>... param1) {
        throw new ReadOnlyFileSystemException();
    }

    @Override
    public void delete(Path param0) {
        throw new ReadOnlyFileSystemException();
    }

    @Override
    public void copy(Path param0, Path param1, CopyOption... param2) {
        throw new ReadOnlyFileSystemException();
    }

    @Override
    public void move(Path param0, Path param1, CopyOption... param2) {
        throw new ReadOnlyFileSystemException();
    }

    @Override
    public boolean isSameFile(Path param0, Path param1) {
        return param0 instanceof LinkFSPath && param1 instanceof LinkFSPath && param0.equals(param1);
    }

    @Override
    public boolean isHidden(Path param0) {
        return false;
    }

    @Override
    public FileStore getFileStore(Path param0) {
        return toLinkPath(param0).getFileSystem().store();
    }

    @Override
    public void checkAccess(Path param0, AccessMode... param1) throws IOException {
        if (param1.length == 0 && !toLinkPath(param0).exists()) {
            throw new NoSuchFileException(param0.toString());
        } else {
            AccessMode[] var3 = param1;
            int var4 = param1.length;
            int var5 = 0;

            while(var5 < var4) {
                AccessMode var0 = var3[var5];
                switch(var0) {
                    case READ:
                        if (!toLinkPath(param0).exists()) {
                            throw new NoSuchFileException(param0.toString());
                        }
                    default:
                        ++var5;
                        break;
                    case EXECUTE:
                    case WRITE:
                        throw new AccessDeniedException(var0.toString());
                }
            }

        }
    }

    @Nullable
    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path param0, Class<V> param1, LinkOption... param2) {
        LinkFSPath var0 = toLinkPath(param0);
        return (V)(param1 == BasicFileAttributeView.class ? var0.getBasicAttributeView() : null);
    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path param0, Class<A> param1, LinkOption... param2) throws IOException {
        LinkFSPath var0 = toLinkPath(param0).toAbsolutePath();
        if (param1 == BasicFileAttributes.class) {
            return (A)var0.getBasicAttributes();
        } else {
            throw new UnsupportedOperationException("Attributes of type " + param1.getName() + " not supported");
        }
    }

    @Override
    public Map<String, Object> readAttributes(Path param0, String param1, LinkOption... param2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAttribute(Path param0, String param1, Object param2, LinkOption... param3) {
        throw new ReadOnlyFileSystemException();
    }

    private static LinkFSPath toLinkPath(@Nullable Path param0) {
        if (param0 == null) {
            throw new NullPointerException();
        } else if (param0 instanceof LinkFSPath) {
            return (LinkFSPath)param0;
        } else {
            throw new ProviderMismatchException();
        }
    }
}

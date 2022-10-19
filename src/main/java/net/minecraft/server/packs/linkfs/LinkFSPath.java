package net.minecraft.server.packs.linkfs;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.ReadOnlyFileSystemException;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

class LinkFSPath implements Path {
    private static final BasicFileAttributes DIRECTORY_ATTRIBUTES = new DummyFileAttributes() {
        @Override
        public boolean isRegularFile() {
            return false;
        }

        @Override
        public boolean isDirectory() {
            return true;
        }
    };
    private static final BasicFileAttributes FILE_ATTRIBUTES = new DummyFileAttributes() {
        @Override
        public boolean isRegularFile() {
            return true;
        }

        @Override
        public boolean isDirectory() {
            return false;
        }
    };
    private static final Comparator<LinkFSPath> PATH_COMPARATOR = Comparator.comparing(LinkFSPath::pathToString);
    private final String name;
    private final LinkFileSystem fileSystem;
    @Nullable
    private final LinkFSPath parent;
    @Nullable
    private List<String> pathToRoot;
    @Nullable
    private String pathString;
    private final PathContents pathContents;

    public LinkFSPath(LinkFileSystem param0, String param1, @Nullable LinkFSPath param2, PathContents param3) {
        this.fileSystem = param0;
        this.name = param1;
        this.parent = param2;
        this.pathContents = param3;
    }

    private LinkFSPath createRelativePath(@Nullable LinkFSPath param0, String param1) {
        return new LinkFSPath(this.fileSystem, param1, param0, PathContents.RELATIVE);
    }

    public LinkFileSystem getFileSystem() {
        return this.fileSystem;
    }

    @Override
    public boolean isAbsolute() {
        return this.pathContents != PathContents.RELATIVE;
    }

    @Override
    public File toFile() {
        PathContents var2 = this.pathContents;
        if (var2 instanceof PathContents.FileContents var0) {
            return var0.contents().toFile();
        } else {
            throw new UnsupportedOperationException("Path " + this.pathToString() + " does not represent file");
        }
    }

    @Nullable
    public LinkFSPath getRoot() {
        return this.isAbsolute() ? this.fileSystem.rootPath() : null;
    }

    public LinkFSPath getFileName() {
        return this.createRelativePath(null, this.name);
    }

    @Nullable
    public LinkFSPath getParent() {
        return this.parent;
    }

    @Override
    public int getNameCount() {
        return this.pathToRoot().size();
    }

    private List<String> pathToRoot() {
        if (this.name.isEmpty()) {
            return List.of();
        } else {
            if (this.pathToRoot == null) {
                Builder<String> var0 = ImmutableList.builder();
                if (this.parent != null) {
                    var0.addAll(this.parent.pathToRoot());
                }

                var0.add(this.name);
                this.pathToRoot = var0.build();
            }

            return this.pathToRoot;
        }
    }

    public LinkFSPath getName(int param0) {
        List<String> var0 = this.pathToRoot();
        if (param0 >= 0 && param0 < var0.size()) {
            return this.createRelativePath(null, var0.get(param0));
        } else {
            throw new IllegalArgumentException("Invalid index: " + param0);
        }
    }

    public LinkFSPath subpath(int param0, int param1) {
        List<String> var0 = this.pathToRoot();
        if (param0 >= 0 && param1 <= var0.size() && param0 < param1) {
            LinkFSPath var1 = null;

            for(int var2 = param0; var2 < param1; ++var2) {
                var1 = this.createRelativePath(var1, var0.get(var2));
            }

            return var1;
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public boolean startsWith(Path param0) {
        if (param0.isAbsolute() != this.isAbsolute()) {
            return false;
        } else if (param0 instanceof LinkFSPath var0) {
            if (var0.fileSystem != this.fileSystem) {
                return false;
            } else {
                List<String> var1 = this.pathToRoot();
                List<String> var2 = var0.pathToRoot();
                int var3 = var2.size();
                if (var3 > var1.size()) {
                    return false;
                } else {
                    for(int var4 = 0; var4 < var3; ++var4) {
                        if (!var2.get(var4).equals(var1.get(var4))) {
                            return false;
                        }
                    }

                    return true;
                }
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean endsWith(Path param0) {
        if (param0.isAbsolute() && !this.isAbsolute()) {
            return false;
        } else if (param0 instanceof LinkFSPath var0) {
            if (var0.fileSystem != this.fileSystem) {
                return false;
            } else {
                List<String> var1 = this.pathToRoot();
                List<String> var2 = var0.pathToRoot();
                int var3 = var2.size();
                int var4 = var1.size() - var3;
                if (var4 < 0) {
                    return false;
                } else {
                    for(int var5 = var3 - 1; var5 >= 0; --var5) {
                        if (!var2.get(var5).equals(var1.get(var4 + var5))) {
                            return false;
                        }
                    }

                    return true;
                }
            }
        } else {
            return false;
        }
    }

    public LinkFSPath normalize() {
        return this;
    }

    public LinkFSPath resolve(Path param0) {
        LinkFSPath var0 = this.toLinkPath(param0);
        return param0.isAbsolute() ? var0 : this.resolve(var0.pathToRoot());
    }

    private LinkFSPath resolve(List<String> param0) {
        LinkFSPath var0 = this;

        for(String var1 : param0) {
            var0 = var0.resolveName(var1);
        }

        return var0;
    }

    LinkFSPath resolveName(String param0) {
        if (isRelativeOrMissing(this.pathContents)) {
            return new LinkFSPath(this.fileSystem, param0, this, this.pathContents);
        } else {
            PathContents var1 = this.pathContents;
            if (var1 instanceof PathContents.DirectoryContents var0) {
                LinkFSPath var1x = var0.children().get(param0);
                return var1x != null ? var1x : new LinkFSPath(this.fileSystem, param0, this, PathContents.MISSING);
            } else if (this.pathContents instanceof PathContents.FileContents) {
                return new LinkFSPath(this.fileSystem, param0, this, PathContents.MISSING);
            } else {
                throw new AssertionError("All content types should be already handled");
            }
        }
    }

    private static boolean isRelativeOrMissing(PathContents param0) {
        return param0 == PathContents.MISSING || param0 == PathContents.RELATIVE;
    }

    public LinkFSPath relativize(Path param0) {
        LinkFSPath var0 = this.toLinkPath(param0);
        if (this.isAbsolute() != var0.isAbsolute()) {
            throw new IllegalArgumentException("absolute mismatch");
        } else {
            List<String> var1 = this.pathToRoot();
            List<String> var2 = var0.pathToRoot();
            if (var1.size() >= var2.size()) {
                throw new IllegalArgumentException();
            } else {
                for(int var3 = 0; var3 < var1.size(); ++var3) {
                    if (!var1.get(var3).equals(var2.get(var3))) {
                        throw new IllegalArgumentException();
                    }
                }

                return var0.subpath(var1.size(), var2.size());
            }
        }
    }

    @Override
    public URI toUri() {
        try {
            return new URI("x-mc-link", this.fileSystem.store().name(), this.pathToString(), null);
        } catch (URISyntaxException var2) {
            throw new AssertionError("Failed to create URI", var2);
        }
    }

    public LinkFSPath toAbsolutePath() {
        return this.isAbsolute() ? this : this.fileSystem.rootPath().resolve(this);
    }

    public LinkFSPath toRealPath(LinkOption... param0) {
        return this.toAbsolutePath();
    }

    @Override
    public WatchKey register(WatchService param0, Kind<?>[] param1, Modifier... param2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int compareTo(Path param0) {
        LinkFSPath var0 = this.toLinkPath(param0);
        return PATH_COMPARATOR.compare(this, var0);
    }

    @Override
    public boolean equals(Object param0) {
        if (param0 == this) {
            return true;
        } else if (param0 instanceof LinkFSPath var0) {
            if (this.fileSystem != var0.fileSystem) {
                return false;
            } else {
                boolean var1 = this.hasRealContents();
                if (var1 != var0.hasRealContents()) {
                    return false;
                } else if (var1) {
                    return this.pathContents == var0.pathContents;
                } else {
                    return Objects.equals(this.parent, var0.parent) && Objects.equals(this.name, var0.name);
                }
            }
        } else {
            return false;
        }
    }

    private boolean hasRealContents() {
        return !isRelativeOrMissing(this.pathContents);
    }

    @Override
    public int hashCode() {
        return this.hasRealContents() ? this.pathContents.hashCode() : this.name.hashCode();
    }

    @Override
    public String toString() {
        return this.pathToString();
    }

    private String pathToString() {
        if (this.pathString == null) {
            StringBuilder var0 = new StringBuilder();
            if (this.isAbsolute()) {
                var0.append("/");
            }

            Joiner.on("/").appendTo(var0, this.pathToRoot());
            this.pathString = var0.toString();
        }

        return this.pathString;
    }

    private LinkFSPath toLinkPath(@Nullable Path param0) {
        if (param0 == null) {
            throw new NullPointerException();
        } else {
            if (param0 instanceof LinkFSPath var0 && var0.fileSystem == this.fileSystem) {
                return var0;
            }

            throw new ProviderMismatchException();
        }
    }

    public boolean exists() {
        return this.hasRealContents();
    }

    @Nullable
    public Path getTargetPath() {
        PathContents var2 = this.pathContents;
        return var2 instanceof PathContents.FileContents var0 ? var0.contents() : null;
    }

    @Nullable
    public PathContents.DirectoryContents getDirectoryContents() {
        PathContents var2 = this.pathContents;
        return var2 instanceof PathContents.DirectoryContents var0 ? var0 : null;
    }

    public BasicFileAttributeView getBasicAttributeView() {
        return new BasicFileAttributeView() {
            @Override
            public String name() {
                return "basic";
            }

            @Override
            public BasicFileAttributes readAttributes() throws IOException {
                return LinkFSPath.this.getBasicAttributes();
            }

            @Override
            public void setTimes(FileTime param0, FileTime param1, FileTime param2) {
                throw new ReadOnlyFileSystemException();
            }
        };
    }

    public BasicFileAttributes getBasicAttributes() throws IOException {
        if (this.pathContents instanceof PathContents.DirectoryContents) {
            return DIRECTORY_ATTRIBUTES;
        } else if (this.pathContents instanceof PathContents.FileContents) {
            return FILE_ATTRIBUTES;
        } else {
            throw new NoSuchFileException(this.pathToString());
        }
    }
}

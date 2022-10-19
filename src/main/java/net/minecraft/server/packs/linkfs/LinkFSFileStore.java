package net.minecraft.server.packs.linkfs;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;
import javax.annotation.Nullable;

class LinkFSFileStore extends FileStore {
    private final String name;

    public LinkFSFileStore(String param0) {
        this.name = param0;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public String type() {
        return "index";
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public long getTotalSpace() {
        return 0L;
    }

    @Override
    public long getUsableSpace() {
        return 0L;
    }

    @Override
    public long getUnallocatedSpace() {
        return 0L;
    }

    @Override
    public boolean supportsFileAttributeView(Class<? extends FileAttributeView> param0) {
        return param0 == BasicFileAttributeView.class;
    }

    @Override
    public boolean supportsFileAttributeView(String param0) {
        return "basic".equals(param0);
    }

    @Nullable
    @Override
    public <V extends FileStoreAttributeView> V getFileStoreAttributeView(Class<V> param0) {
        return null;
    }

    @Override
    public Object getAttribute(String param0) throws IOException {
        throw new UnsupportedOperationException();
    }
}

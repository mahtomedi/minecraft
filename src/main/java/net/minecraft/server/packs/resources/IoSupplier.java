package net.minecraft.server.packs.resources;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@FunctionalInterface
public interface IoSupplier<T> {
    static IoSupplier<InputStream> create(Path param0) {
        return () -> Files.newInputStream(param0);
    }

    static IoSupplier<InputStream> create(ZipFile param0, ZipEntry param1) {
        return () -> param0.getInputStream(param1);
    }

    T get() throws IOException;
}

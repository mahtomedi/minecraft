package net.minecraft.util;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.Util;
import org.slf4j.Logger;

public class FileZipper implements Closeable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Path outputFile;
    private final Path tempFile;
    private final FileSystem fs;

    public FileZipper(Path param0) {
        this.outputFile = param0;
        this.tempFile = param0.resolveSibling(param0.getFileName().toString() + "_tmp");

        try {
            this.fs = Util.ZIP_FILE_SYSTEM_PROVIDER.newFileSystem(this.tempFile, ImmutableMap.of("create", "true"));
        } catch (IOException var3) {
            throw new UncheckedIOException(var3);
        }
    }

    public void add(Path param0, String param1) {
        try {
            Path var0 = this.fs.getPath(File.separator);
            Path var1 = var0.resolve(param0.toString());
            Files.createDirectories(var1.getParent());
            Files.write(var1, param1.getBytes(StandardCharsets.UTF_8));
        } catch (IOException var5) {
            throw new UncheckedIOException(var5);
        }
    }

    public void add(Path param0, File param1) {
        try {
            Path var0 = this.fs.getPath(File.separator);
            Path var1 = var0.resolve(param0.toString());
            Files.createDirectories(var1.getParent());
            Files.copy(param1.toPath(), var1);
        } catch (IOException var5) {
            throw new UncheckedIOException(var5);
        }
    }

    public void add(Path param0) {
        try {
            Path var0 = this.fs.getPath(File.separator);
            if (Files.isRegularFile(param0)) {
                Path var1 = var0.resolve(param0.getParent().relativize(param0).toString());
                Files.copy(var1, param0);
            } else {
                try (Stream<Path> var2 = Files.find(param0, Integer.MAX_VALUE, (param0x, param1) -> param1.isRegularFile())) {
                    for(Path var3 : var2.collect(Collectors.toList())) {
                        Path var4 = var0.resolve(param0.relativize(var3).toString());
                        Files.createDirectories(var4.getParent());
                        Files.copy(var3, var4);
                    }
                }

            }
        } catch (IOException var9) {
            throw new UncheckedIOException(var9);
        }
    }

    @Override
    public void close() {
        try {
            this.fs.close();
            Files.move(this.tempFile, this.outputFile);
            LOGGER.info("Compressed to {}", this.outputFile);
        } catch (IOException var2) {
            throw new UncheckedIOException(var2);
        }
    }
}

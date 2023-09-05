package net.minecraft.data.structures;

import com.google.common.collect.Lists;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class SnbtToNbt implements DataProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final PackOutput output;
    private final Iterable<Path> inputFolders;
    private final List<SnbtToNbt.Filter> filters = Lists.newArrayList();

    public SnbtToNbt(PackOutput param0, Iterable<Path> param1) {
        this.output = param0;
        this.inputFolders = param1;
    }

    public SnbtToNbt addFilter(SnbtToNbt.Filter param0) {
        this.filters.add(param0);
        return this;
    }

    private CompoundTag applyFilters(String param0, CompoundTag param1) {
        CompoundTag var0 = param1;

        for(SnbtToNbt.Filter var1 : this.filters) {
            var0 = var1.apply(param0, var0);
        }

        return var0;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput param0) {
        Path var0 = this.output.getOutputFolder();
        List<CompletableFuture<?>> var1 = Lists.newArrayList();

        for(Path var2 : this.inputFolders) {
            var1.add(
                CompletableFuture.<CompletableFuture>supplyAsync(
                        () -> {
                            try {
                                CompletableFuture var5x;
                                try (Stream<Path> var0x = Files.walk(var2)) {
                                    var5x = CompletableFuture.allOf(
                                        var0x.filter(param0x -> param0x.toString().endsWith(".snbt")).map(param3 -> CompletableFuture.runAsync(() -> {
                                                SnbtToNbt.TaskResult var0x = this.readStructure(param3, this.getName(var2, param3));
                                                this.storeStructureIfChanged(param0, var0x, var0);
                                            }, Util.backgroundExecutor())).toArray(param0x -> new CompletableFuture[param0x])
                                    );
                                }
            
                                return var5x;
                            } catch (Exception var9) {
                                throw new RuntimeException("Failed to read structure input directory, aborting", var9);
                            }
                        },
                        Util.backgroundExecutor()
                    )
                    .thenCompose(param0x -> param0x)
            );
        }

        return Util.sequenceFailFast(var1);
    }

    @Override
    public final String getName() {
        return "SNBT -> NBT";
    }

    private String getName(Path param0, Path param1) {
        String var0 = param0.relativize(param1).toString().replaceAll("\\\\", "/");
        return var0.substring(0, var0.length() - ".snbt".length());
    }

    private SnbtToNbt.TaskResult readStructure(Path param0, String param1) {
        try {
            SnbtToNbt.TaskResult var10;
            try (BufferedReader var0 = Files.newBufferedReader(param0)) {
                String var1 = IOUtils.toString((Reader)var0);
                CompoundTag var2 = this.applyFilters(param1, NbtUtils.snbtToStructure(var1));
                ByteArrayOutputStream var3 = new ByteArrayOutputStream();
                HashingOutputStream var4 = new HashingOutputStream(Hashing.sha1(), var3);
                NbtIo.writeCompressed(var2, var4);
                byte[] var5 = var3.toByteArray();
                HashCode var6 = var4.hash();
                var10 = new SnbtToNbt.TaskResult(param1, var5, var6);
            }

            return var10;
        } catch (Throwable var13) {
            throw new SnbtToNbt.StructureConversionException(param0, var13);
        }
    }

    private void storeStructureIfChanged(CachedOutput param0, SnbtToNbt.TaskResult param1, Path param2) {
        Path var0 = param2.resolve(param1.name + ".nbt");

        try {
            param0.writeIfNeeded(var0, param1.payload, param1.hash);
        } catch (IOException var6) {
            LOGGER.error("Couldn't write structure {} at {}", param1.name, var0, var6);
        }

    }

    @FunctionalInterface
    public interface Filter {
        CompoundTag apply(String var1, CompoundTag var2);
    }

    static class StructureConversionException extends RuntimeException {
        public StructureConversionException(Path param0, Throwable param1) {
            super(param0.toAbsolutePath().toString(), param1);
        }
    }

    static record TaskResult(String name, byte[] payload, HashCode hash) {
    }
}

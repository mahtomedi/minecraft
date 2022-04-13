package net.minecraft.data.structures;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class SnbtToNbt implements DataProvider {
    @Nullable
    private static final Path DUMP_SNBT_TO = null;
    private static final Logger LOGGER = LogUtils.getLogger();
    private final DataGenerator generator;
    private final List<SnbtToNbt.Filter> filters = Lists.newArrayList();

    public SnbtToNbt(DataGenerator param0) {
        this.generator = param0;
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
    public void run(CachedOutput param0) throws IOException {
        Path var0 = this.generator.getOutputFolder();
        List<CompletableFuture<SnbtToNbt.TaskResult>> var1 = Lists.newArrayList();

        for(Path var2 : this.generator.getInputFolders()) {
            Files.walk(var2)
                .filter(param0x -> param0x.toString().endsWith(".snbt"))
                .forEach(
                    param2 -> var1.add(CompletableFuture.supplyAsync(() -> this.readStructure(param2, this.getName(var2, param2)), Util.backgroundExecutor()))
                );
        }

        boolean var3 = false;

        for(CompletableFuture<SnbtToNbt.TaskResult> var4 : var1) {
            try {
                this.storeStructureIfChanged(param0, var4.get(), var0);
            } catch (Exception var8) {
                LOGGER.error("Failed to process structure", (Throwable)var8);
                var3 = true;
            }
        }

        if (var3) {
            throw new IllegalStateException("Failed to convert all structures, aborting");
        }
    }

    @Override
    public String getName() {
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
                NbtIo.writeCompressed(var2, var3);
                byte[] var4 = var3.toByteArray();
                String var5 = SHA1.hashBytes(var4).toString();
                String var6;
                if (DUMP_SNBT_TO != null) {
                    var6 = NbtUtils.structureToSnbt(var2);
                } else {
                    var6 = null;
                }

                var10 = new SnbtToNbt.TaskResult(param1, var4, var6, var5);
            }

            return var10;
        } catch (Throwable var13) {
            throw new SnbtToNbt.StructureConversionException(param0, var13);
        }
    }

    private void storeStructureIfChanged(CachedOutput param0, SnbtToNbt.TaskResult param1, Path param2) {
        if (param1.snbtPayload != null) {
            Path var0 = DUMP_SNBT_TO.resolve(param1.name + ".snbt");

            try {
                NbtToSnbt.writeSnbt(var0, param1.snbtPayload);
            } catch (IOException var7) {
                LOGGER.error("Couldn't write structure SNBT {} at {}", param1.name, var0, var7);
            }
        }

        Path var2 = param2.resolve(param1.name + ".nbt");

        try {
            param0.writeIfNeeded(var2, param1.payload, param1.hash);
        } catch (IOException var6) {
            LOGGER.error("Couldn't write structure {} at {}", param1.name, var2, var6);
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

    static class TaskResult {
        final String name;
        final byte[] payload;
        @Nullable
        final String snbtPayload;
        final String hash;

        public TaskResult(String param0, byte[] param1, @Nullable String param2, String param3) {
            this.name = param0;
            this.payload = param1;
            this.snbtPayload = param2;
            this.hash = param3;
        }
    }
}

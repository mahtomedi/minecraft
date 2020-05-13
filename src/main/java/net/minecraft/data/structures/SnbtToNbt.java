package net.minecraft.data.structures;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.TagParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SnbtToNbt implements DataProvider {
    @Nullable
    private static final Path dumpSnbtTo = null;
    private static final Logger LOGGER = LogManager.getLogger();
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
    public void run(HashCache param0) throws IOException {
        Path var0 = this.generator.getOutputFolder();
        List<CompletableFuture<SnbtToNbt.TaskResult>> var1 = Lists.newArrayList();

        for(Path var2 : this.generator.getInputFolders()) {
            Files.walk(var2)
                .filter(param0x -> param0x.toString().endsWith(".snbt"))
                .forEach(
                    param2 -> var1.add(CompletableFuture.supplyAsync(() -> this.readStructure(param2, this.getName(var2, param2)), Util.backgroundExecutor()))
                );
        }

        Util.sequence(var1).join().stream().filter(Objects::nonNull).forEach(param2 -> this.storeStructureIfChanged(param0, param2, var0));
    }

    @Override
    public String getName() {
        return "SNBT -> NBT";
    }

    private String getName(Path param0, Path param1) {
        String var0 = param0.relativize(param1).toString().replaceAll("\\\\", "/");
        return var0.substring(0, var0.length() - ".snbt".length());
    }

    @Nullable
    private SnbtToNbt.TaskResult readStructure(Path param0, String param1) {
        try (BufferedReader var0 = Files.newBufferedReader(param0)) {
            String var1 = IOUtils.toString((Reader)var0);
            CompoundTag var2 = this.applyFilters(param1, TagParser.parseTag(var1));
            ByteArrayOutputStream var3 = new ByteArrayOutputStream();
            NbtIo.writeCompressed(var2, var3);
            byte[] var4 = var3.toByteArray();
            String var5 = SHA1.hashBytes(var4).toString();
            String var6;
            if (dumpSnbtTo != null) {
                var6 = var2.getPrettyDisplay("    ", 0).getString() + "\n";
            } else {
                var6 = null;
            }

            return new SnbtToNbt.TaskResult(param1, var4, var6, var5);
        } catch (CommandSyntaxException var24) {
            LOGGER.error("Couldn't convert {} from SNBT to NBT at {} as it's invalid SNBT", param1, param0, var24);
        } catch (IOException var25) {
            LOGGER.error("Couldn't convert {} from SNBT to NBT at {}", param1, param0, var25);
        }

        return null;
    }

    private void storeStructureIfChanged(HashCache param0, SnbtToNbt.TaskResult param1, Path param2) {
        if (param1.snbtPayload != null) {
            Path var0 = dumpSnbtTo.resolve(param1.name + ".snbt");

            try {
                FileUtils.write(var0.toFile(), param1.snbtPayload, StandardCharsets.UTF_8);
            } catch (IOException var18) {
                LOGGER.error("Couldn't write structure SNBT {} at {}", param1.name, var0, var18);
            }
        }

        Path var2 = param2.resolve(param1.name + ".nbt");

        try {
            if (!Objects.equals(param0.getHash(var2), param1.hash) || !Files.exists(var2)) {
                Files.createDirectories(var2.getParent());

                try (OutputStream var3 = Files.newOutputStream(var2)) {
                    var3.write(param1.payload);
                }
            }

            param0.putNew(var2, param1.hash);
        } catch (IOException var20) {
            LOGGER.error("Couldn't write structure {} at {}", param1.name, var2, var20);
        }

    }

    @FunctionalInterface
    public interface Filter {
        CompoundTag apply(String var1, CompoundTag var2);
    }

    static class TaskResult {
        private final String name;
        private final byte[] payload;
        @Nullable
        private final String snbtPayload;
        private final String hash;

        public TaskResult(String param0, byte[] param1, @Nullable String param2, String param3) {
            this.name = param0;
            this.payload = param1;
            this.snbtPayload = param2;
            this.hash = param3;
        }
    }
}

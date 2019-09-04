package net.minecraft.data.structures;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
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
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SnbtToNbt implements DataProvider {
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
            ByteArrayOutputStream var2 = new ByteArrayOutputStream();
            NbtIo.writeCompressed(this.applyFilters(param1, TagParser.parseTag(var1)), var2);
            byte[] var3 = var2.toByteArray();
            String var4 = SHA1.hashBytes(var3).toString();
            return new SnbtToNbt.TaskResult(param1, var3, var4);
        } catch (CommandSyntaxException var22) {
            LOGGER.error("Couldn't convert {} from SNBT to NBT at {} as it's invalid SNBT", param1, param0, var22);
        } catch (IOException var23) {
            LOGGER.error("Couldn't convert {} from SNBT to NBT at {}", param1, param0, var23);
        }

        return null;
    }

    private void storeStructureIfChanged(HashCache param0, SnbtToNbt.TaskResult param1, Path param2) {
        Path var0 = param2.resolve(param1.name + ".nbt");

        try {
            if (!Objects.equals(param0.getHash(var0), param1.hash) || !Files.exists(var0)) {
                Files.createDirectories(var0.getParent());

                try (OutputStream var1 = Files.newOutputStream(var0)) {
                    var1.write(param1.payload);
                }
            }

            param0.putNew(var0, param1.hash);
        } catch (IOException var18) {
            LOGGER.error("Couldn't write structure {} at {}", param1.name, var0, var18);
        }

    }

    @FunctionalInterface
    public interface Filter {
        CompoundTag apply(String var1, CompoundTag var2);
    }

    static class TaskResult {
        private final String name;
        private final byte[] payload;
        private final String hash;

        public TaskResult(String param0, byte[] param1, String param2) {
            this.name = param0;
            this.payload = param1;
            this.hash = param2;
        }
    }
}

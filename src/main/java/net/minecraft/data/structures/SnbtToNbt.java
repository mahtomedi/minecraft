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

        for(Path var1 : this.generator.getInputFolders()) {
            Files.walk(var1)
                .filter(param0x -> param0x.toString().endsWith(".snbt"))
                .forEach(param3 -> this.convertStructure(param0, param3, this.getName(var1, param3), var0));
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

    private void convertStructure(HashCache param0, Path param1, String param2, Path param3) {
        try {
            Path var0 = param3.resolve(param2 + ".nbt");

            try (BufferedReader var1 = Files.newBufferedReader(param1)) {
                String var2 = IOUtils.toString((Reader)var1);
                ByteArrayOutputStream var3 = new ByteArrayOutputStream();
                NbtIo.writeCompressed(this.applyFilters(param2, TagParser.parseTag(var2)), var3);
                String var4 = SHA1.hashBytes(var3.toByteArray()).toString();
                if (!Objects.equals(param0.getHash(var0), var4) || !Files.exists(var0)) {
                    Files.createDirectories(var0.getParent());

                    try (OutputStream var5 = Files.newOutputStream(var0)) {
                        var5.write(var3.toByteArray());
                    }
                }

                param0.putNew(var0, var4);
            }
        } catch (CommandSyntaxException var43) {
            LOGGER.error("Couldn't convert {} from SNBT to NBT at {} as it's invalid SNBT", param2, param1, var43);
        } catch (IOException var44) {
            LOGGER.error("Couldn't convert {} from SNBT to NBT at {}", param2, param1, var44);
        }

    }

    @FunctionalInterface
    public interface Filter {
        CompoundTag apply(String var1, CompoundTag var2);
    }
}

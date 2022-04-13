package net.minecraft.data.structures;

import com.mojang.logging.LogUtils;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.Nullable;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import org.slf4j.Logger;

public class NbtToSnbt implements DataProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final DataGenerator generator;

    public NbtToSnbt(DataGenerator param0) {
        this.generator = param0;
    }

    @Override
    public void run(CachedOutput param0) throws IOException {
        Path var0 = this.generator.getOutputFolder();

        for(Path var1 : this.generator.getInputFolders()) {
            Files.walk(var1)
                .filter(param0x -> param0x.toString().endsWith(".nbt"))
                .forEach(param2 -> convertStructure(param2, this.getName(var1, param2), var0));
        }

    }

    @Override
    public String getName() {
        return "NBT to SNBT";
    }

    private String getName(Path param0, Path param1) {
        String var0 = param0.relativize(param1).toString().replaceAll("\\\\", "/");
        return var0.substring(0, var0.length() - ".nbt".length());
    }

    @Nullable
    public static Path convertStructure(Path param0, String param1, Path param2) {
        try {
            writeSnbt(param2.resolve(param1 + ".snbt"), NbtUtils.structureToSnbt(NbtIo.readCompressed(Files.newInputStream(param0))));
            LOGGER.info("Converted {} from NBT to SNBT", param1);
            return param2.resolve(param1 + ".snbt");
        } catch (IOException var4) {
            LOGGER.error("Couldn't convert {} from NBT to SNBT at {}", param1, param0, var4);
            return null;
        }
    }

    public static void writeSnbt(Path param0, String param1) throws IOException {
        Files.createDirectories(param0.getParent());

        try (BufferedWriter var0 = Files.newBufferedWriter(param0)) {
            var0.write(param1);
            var0.write(10);
        }

    }
}

package net.minecraft.data.structures;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NbtToSnbt implements DataProvider {
    private static final Logger LOGGER = LogManager.getLogger();
    private final DataGenerator generator;

    public NbtToSnbt(DataGenerator param0) {
        this.generator = param0;
    }

    @Override
    public void run(HashCache param0) throws IOException {
        Path var0 = this.generator.getOutputFolder();

        for(Path var1 : this.generator.getInputFolders()) {
            Files.walk(var1)
                .filter(param0x -> param0x.toString().endsWith(".nbt"))
                .forEach(param2 -> this.convertStructure(param2, this.getName(var1, param2), var0));
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

    private void convertStructure(Path param0, String param1, Path param2) {
        try {
            CompoundTag var0 = NbtIo.readCompressed(Files.newInputStream(param0));
            Component var1 = var0.getPrettyDisplay("    ", 0);
            String var2 = var1.getString() + "\n";
            Path var3 = param2.resolve(param1 + ".snbt");
            Files.createDirectories(var3.getParent());

            try (BufferedWriter var4 = Files.newBufferedWriter(var3)) {
                var4.write(var2);
            }

            LOGGER.info("Converted {} from NBT to SNBT", param1);
        } catch (IOException var21) {
            LOGGER.error("Couldn't convert {} from NBT to SNBT at {}", param1, param0, var21);
        }

    }
}

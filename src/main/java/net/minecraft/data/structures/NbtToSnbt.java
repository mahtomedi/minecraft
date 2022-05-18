package net.minecraft.data.structures;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.mojang.logging.LogUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
                .forEach(param3 -> convertStructure(param0, param3, this.getName(var1, param3), var0));
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
    public static Path convertStructure(CachedOutput param0, Path param1, String param2, Path param3) {
        try {
            Path var6;
            try (InputStream var0 = Files.newInputStream(param1)) {
                Path var1 = param3.resolve(param2 + ".snbt");
                writeSnbt(param0, var1, NbtUtils.structureToSnbt(NbtIo.readCompressed(var0)));
                LOGGER.info("Converted {} from NBT to SNBT", param2);
                var6 = var1;
            }

            return var6;
        } catch (IOException var9) {
            LOGGER.error("Couldn't convert {} from NBT to SNBT at {}", param2, param1, var9);
            return null;
        }
    }

    public static void writeSnbt(CachedOutput param0, Path param1, String param2) throws IOException {
        ByteArrayOutputStream var0 = new ByteArrayOutputStream();
        HashingOutputStream var1 = new HashingOutputStream(Hashing.sha1(), var0);
        var1.write(param2.getBytes(StandardCharsets.UTF_8));
        var1.write(10);
        param0.writeIfNeeded(param1, var0.toByteArray(), var1.hash());
    }
}

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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import org.slf4j.Logger;

public class NbtToSnbt implements DataProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Iterable<Path> inputFolders;
    private final PackOutput output;

    public NbtToSnbt(PackOutput param0, Collection<Path> param1) {
        this.inputFolders = param1;
        this.output = param0;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput param0) {
        Path var0 = this.output.getOutputFolder();
        List<CompletableFuture<?>> var1 = new ArrayList<>();

        for(Path var2 : this.inputFolders) {
            var1.add(
                CompletableFuture.<CompletableFuture>supplyAsync(
                        () -> {
                            try {
                                CompletableFuture var4;
                                try (Stream<Path> var0x = Files.walk(var2)) {
                                    var4 = CompletableFuture.allOf(
                                        var0x.filter(param0x -> param0x.toString().endsWith(".nbt"))
                                            .map(
                                                param3 -> CompletableFuture.runAsync(
                                                        () -> convertStructure(param0, param3, getName(var2, param3), var0), Util.ioPool()
                                                    )
                                            )
                                            .toArray(param0x -> new CompletableFuture[param0x])
                                    );
                                }
            
                                return var4;
                            } catch (IOException var8) {
                                LOGGER.error("Failed to read structure input directory", (Throwable)var8);
                                return CompletableFuture.completedFuture(null);
                            }
                        },
                        Util.backgroundExecutor()
                    )
                    .thenCompose(param0x -> param0x)
            );
        }

        return CompletableFuture.allOf(var1.toArray(param0x -> new CompletableFuture[param0x]));
    }

    @Override
    public final String getName() {
        return "NBT -> SNBT";
    }

    private static String getName(Path param0, Path param1) {
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

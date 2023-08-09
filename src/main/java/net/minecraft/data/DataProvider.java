package net.minecraft.data;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.function.ToIntFunction;
import net.minecraft.Util;
import net.minecraft.util.GsonHelper;
import org.slf4j.Logger;

public interface DataProvider {
    ToIntFunction<String> FIXED_ORDER_FIELDS = Util.make(new Object2IntOpenHashMap<>(), param0 -> {
        param0.put("type", 0);
        param0.put("parent", 1);
        param0.defaultReturnValue(2);
    });
    Comparator<String> KEY_COMPARATOR = Comparator.comparingInt(FIXED_ORDER_FIELDS).thenComparing(param0 -> param0);
    Logger LOGGER = LogUtils.getLogger();

    CompletableFuture<?> run(CachedOutput var1);

    String getName();

    static <T> CompletableFuture<?> saveStable(CachedOutput param0, Codec<T> param1, T param2, Path param3) {
        JsonElement var0 = Util.getOrThrow(param1.encodeStart(JsonOps.INSTANCE, param2), IllegalStateException::new);
        return saveStable(param0, var0, param3);
    }

    static CompletableFuture<?> saveStable(CachedOutput param0, JsonElement param1, Path param2) {
        return CompletableFuture.runAsync(() -> {
            try {
                ByteArrayOutputStream var0 = new ByteArrayOutputStream();
                HashingOutputStream var1x = new HashingOutputStream(Hashing.sha1(), var0);

                try (JsonWriter var2x = new JsonWriter(new OutputStreamWriter(var1x, StandardCharsets.UTF_8))) {
                    var2x.setSerializeNulls(false);
                    var2x.setIndent("  ");
                    GsonHelper.writeValue(var2x, param1, KEY_COMPARATOR);
                }

                param0.writeIfNeeded(param2, var0.toByteArray(), var1x.hash());
            } catch (IOException var10) {
                LOGGER.error("Failed to save file to {}", param2, var10);
            }

        }, Util.backgroundExecutor());
    }

    @FunctionalInterface
    public interface Factory<T extends DataProvider> {
        T create(PackOutput var1);
    }
}

package net.minecraft.util.eventlog;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import javax.annotation.Nullable;
import net.minecraft.Util;

public interface JsonEventLogReader<T> extends Closeable {
    static <T> JsonEventLogReader<T> create(final Codec<T> param0, Reader param1) {
        final JsonReader var0 = new JsonReader(param1);
        var0.setLenient(true);
        return new JsonEventLogReader<T>() {
            @Nullable
            @Override
            public T next() throws IOException {
                try {
                    if (!var0.hasNext()) {
                        return null;
                    } else {
                        JsonElement var0 = JsonParser.parseReader(var0);
                        return Util.getOrThrow(param0.parse(JsonOps.INSTANCE, var0), IOException::new);
                    }
                } catch (JsonParseException var21) {
                    throw new IOException(var21);
                } catch (EOFException var3) {
                    return null;
                }
            }

            @Override
            public void close() throws IOException {
                var0.close();
            }
        };
    }

    @Nullable
    T next() throws IOException;
}

package net.minecraft.util.eventlog;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import net.minecraft.Util;

public class JsonEventLog<T> implements Closeable {
    private static final Gson GSON = new Gson();
    private final Codec<T> codec;
    final FileChannel channel;
    private final AtomicInteger referenceCount = new AtomicInteger(1);

    public JsonEventLog(Codec<T> param0, FileChannel param1) {
        this.codec = param0;
        this.channel = param1;
    }

    public static <T> JsonEventLog<T> open(Codec<T> param0, Path param1) throws IOException {
        FileChannel var0 = FileChannel.open(param1, StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);
        return new JsonEventLog<>(param0, var0);
    }

    public void write(T param0) throws IOException, JsonIOException {
        JsonElement var0 = Util.getOrThrow(this.codec.encodeStart(JsonOps.INSTANCE, param0), IOException::new);
        this.channel.position(this.channel.size());
        Writer var1 = Channels.newWriter(this.channel, StandardCharsets.UTF_8);
        GSON.toJson(var0, var1);
        var1.write(10);
        var1.flush();
    }

    public JsonEventLogReader<T> openReader() throws IOException {
        if (this.referenceCount.get() <= 0) {
            throw new IOException("Event log has already been closed");
        } else {
            this.referenceCount.incrementAndGet();
            final JsonEventLogReader<T> var0 = JsonEventLogReader.create(this.codec, Channels.newReader(this.channel, StandardCharsets.UTF_8));
            return new JsonEventLogReader<T>() {
                private volatile long position;

                @Nullable
                @Override
                public T next() throws IOException {
                    Object var1;
                    try {
                        JsonEventLog.this.channel.position(this.position);
                        var1 = var0.next();
                    } finally {
                        this.position = JsonEventLog.this.channel.position();
                    }

                    return (T)var1;
                }

                @Override
                public void close() throws IOException {
                    JsonEventLog.this.releaseReference();
                }
            };
        }
    }

    @Override
    public void close() throws IOException {
        this.releaseReference();
    }

    void releaseReference() throws IOException {
        if (this.referenceCount.decrementAndGet() <= 0) {
            this.channel.close();
        }

    }
}

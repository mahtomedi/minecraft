package net.minecraft.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.StringReader;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.lang.reflect.Field;
import net.minecraft.Util;

public class ParserUtils {
    private static final Field JSON_READER_POS = Util.make(() -> {
        try {
            Field var0 = JsonReader.class.getDeclaredField("pos");
            var0.setAccessible(true);
            return var0;
        } catch (NoSuchFieldException var11) {
            throw new IllegalStateException("Couldn't get field 'pos' for JsonReader", var11);
        }
    });
    private static final Field JSON_READER_LINESTART = Util.make(() -> {
        try {
            Field var0 = JsonReader.class.getDeclaredField("lineStart");
            var0.setAccessible(true);
            return var0;
        } catch (NoSuchFieldException var11) {
            throw new IllegalStateException("Couldn't get field 'lineStart' for JsonReader", var11);
        }
    });

    private static int getPos(JsonReader param0) {
        try {
            return JSON_READER_POS.getInt(param0) - JSON_READER_LINESTART.getInt(param0) + 1;
        } catch (IllegalAccessException var2) {
            throw new IllegalStateException("Couldn't read position of JsonReader", var2);
        }
    }

    public static <T> T parseJson(StringReader param0, Codec<T> param1) {
        JsonReader var0 = new JsonReader(new java.io.StringReader(param0.getRemaining()));
        var0.setLenient(false);

        Object var4;
        try {
            JsonElement var1 = Streams.parse(var0);
            var4 = Util.<T, JsonParseException>getOrThrow(param1.parse(JsonOps.INSTANCE, var1), JsonParseException::new);
        } catch (StackOverflowError var8) {
            throw new JsonParseException(var8);
        } finally {
            param0.setCursor(param0.getCursor() + getPos(var0));
        }

        return (T)var4;
    }
}

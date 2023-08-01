package net.minecraft.server.packs.metadata;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.Util;

public interface MetadataSectionType<T> extends MetadataSectionSerializer<T> {
    JsonObject toJson(T var1);

    static <T> MetadataSectionType<T> fromCodec(final String param0, final Codec<T> param1) {
        return new MetadataSectionType<T>() {
            @Override
            public String getMetadataSectionName() {
                return param0;
            }

            @Override
            public T fromJson(JsonObject param0x) {
                return Util.getOrThrow(param1.parse(JsonOps.INSTANCE, param0), JsonParseException::new);
            }

            @Override
            public JsonObject toJson(T param0x) {
                return Util.getOrThrow(param1.encodeStart(JsonOps.INSTANCE, param0), IllegalArgumentException::new).getAsJsonObject();
            }
        };
    }
}

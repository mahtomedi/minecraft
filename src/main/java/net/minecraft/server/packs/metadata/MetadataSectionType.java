package net.minecraft.server.packs.metadata;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

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
                return param1.parse(JsonOps.INSTANCE, param0).getOrThrow(false, param0xx -> {
                });
            }

            @Override
            public JsonObject toJson(T param0x) {
                return param1.encodeStart(JsonOps.INSTANCE, param0).getOrThrow(false, param0xx -> {
                }).getAsJsonObject();
            }
        };
    }
}

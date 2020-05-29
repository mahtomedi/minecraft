package net.minecraft.world.level.storage.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import java.lang.reflect.Type;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class GsonAdapterFactory {
    public static <E, T extends SerializerType<E>> GsonAdapterFactory.Builder<E, T> builder(
        Registry<T> param0, String param1, String param2, Function<E, T> param3
    ) {
        return new GsonAdapterFactory.Builder<>(param0, param1, param2, param3);
    }

    public static class Builder<E, T extends SerializerType<E>> {
        private final Registry<T> registry;
        private final String elementName;
        private final String typeKey;
        private final Function<E, T> typeGetter;
        @Nullable
        private Pair<T, GsonAdapterFactory.DefaultSerializer<? extends E>> defaultType;

        private Builder(Registry<T> param0, String param1, String param2, Function<E, T> param3) {
            this.registry = param0;
            this.elementName = param1;
            this.typeKey = param2;
            this.typeGetter = param3;
        }

        public Object build() {
            return new GsonAdapterFactory.JsonAdapter(this.registry, this.elementName, this.typeKey, this.typeGetter, this.defaultType);
        }
    }

    public interface DefaultSerializer<T> {
        JsonElement serialize(T var1, JsonSerializationContext var2);

        T deserialize(JsonElement var1, JsonDeserializationContext var2);
    }

    static class JsonAdapter<E, T extends SerializerType<E>> implements JsonDeserializer<E>, JsonSerializer<E> {
        private final Registry<T> registry;
        private final String elementName;
        private final String typeKey;
        private final Function<E, T> typeGetter;
        @Nullable
        private final Pair<T, GsonAdapterFactory.DefaultSerializer<? extends E>> defaultType;

        private JsonAdapter(
            Registry<T> param0,
            String param1,
            String param2,
            Function<E, T> param3,
            @Nullable Pair<T, GsonAdapterFactory.DefaultSerializer<? extends E>> param4
        ) {
            this.registry = param0;
            this.elementName = param1;
            this.typeKey = param2;
            this.typeGetter = param3;
            this.defaultType = param4;
        }

        @Override
        public E deserialize(JsonElement param0, Type param1, JsonDeserializationContext param2) throws JsonParseException {
            if (param0.isJsonObject()) {
                JsonObject var0 = GsonHelper.convertToJsonObject(param0, this.elementName);
                ResourceLocation var1 = new ResourceLocation(GsonHelper.getAsString(var0, this.typeKey));
                T var2 = this.registry.get(var1);
                if (var2 == null) {
                    throw new JsonSyntaxException("Unknown type '" + var1 + "'");
                } else {
                    return var2.getSerializer().deserialize(var0, param2);
                }
            } else if (this.defaultType == null) {
                throw new UnsupportedOperationException("Object " + param0 + " can't be deserialized");
            } else {
                return this.defaultType.getSecond().deserialize(param0, param2);
            }
        }

        @Override
        public JsonElement serialize(E param0, Type param1, JsonSerializationContext param2) {
            T var0 = this.typeGetter.apply(param0);
            if (this.defaultType != null && this.defaultType.getFirst() == var0) {
                return this.defaultType.getSecond().serialize(param0, param2);
            } else if (var0 == null) {
                throw new JsonSyntaxException("Unknown type: " + param0);
            } else {
                JsonObject var1 = new JsonObject();
                var1.addProperty(this.typeKey, this.registry.getKey(var0).toString());
                var0.getSerializer().serialize(var1, param0, param2);
                return var1;
            }
        }
    }
}

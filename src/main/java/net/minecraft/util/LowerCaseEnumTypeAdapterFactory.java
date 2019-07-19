package net.minecraft.util;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;

public class LowerCaseEnumTypeAdapterFactory implements TypeAdapterFactory {
    @Nullable
    @Override
    public <T> TypeAdapter<T> create(Gson param0, TypeToken<T> param1) {
        Class<T> var0 = (Class<T>)param1.getRawType();
        if (!var0.isEnum()) {
            return null;
        } else {
            final Map<String, T> var1 = Maps.newHashMap();

            for(T var2 : var0.getEnumConstants()) {
                var1.put(this.toLowercase(var2), var2);
            }

            return new TypeAdapter<T>() {
                @Override
                public void write(JsonWriter param0, T param1) throws IOException {
                    if (param1 == null) {
                        param0.nullValue();
                    } else {
                        param0.value(LowerCaseEnumTypeAdapterFactory.this.toLowercase(param1));
                    }

                }

                @Nullable
                @Override
                public T read(JsonReader param0) throws IOException {
                    if (param0.peek() == JsonToken.NULL) {
                        param0.nextNull();
                        return null;
                    } else {
                        return var1.get(param0.nextString());
                    }
                }
            };
        }
    }

    private String toLowercase(Object param0) {
        return param0 instanceof Enum ? ((Enum)param0).name().toLowerCase(Locale.ROOT) : param0.toString().toLowerCase(Locale.ROOT);
    }
}

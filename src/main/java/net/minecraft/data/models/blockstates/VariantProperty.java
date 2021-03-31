package net.minecraft.data.models.blockstates;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.function.Function;

public class VariantProperty<T> {
    private final String key;
    private final Function<T, JsonElement> serializer;

    public VariantProperty(String param0, Function<T, JsonElement> param1) {
        this.key = param0;
        this.serializer = param1;
    }

    public VariantProperty<T>.Value withValue(T param0) {
        return new VariantProperty.Value(param0);
    }

    @Override
    public String toString() {
        return this.key;
    }

    public class Value {
        private final T value;

        public Value(T param1) {
            this.value = param1;
        }

        public VariantProperty<T> getKey() {
            return VariantProperty.this;
        }

        public void addToVariant(JsonObject param0) {
            param0.add(VariantProperty.this.key, VariantProperty.this.serializer.apply(this.value));
        }

        @Override
        public String toString() {
            return VariantProperty.this.key + "=" + this.value;
        }
    }
}

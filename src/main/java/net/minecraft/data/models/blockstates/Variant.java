package net.minecraft.data.models.blockstates;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class Variant implements Supplier<JsonElement> {
    private final Map<VariantProperty<?>, VariantProperty<?>.Value> values = Maps.newLinkedHashMap();

    public <T> Variant with(VariantProperty<T> param0, T param1) {
        VariantProperty<?>.Value var0 = this.values.put(param0, param0.withValue(param1));
        if (var0 != null) {
            throw new IllegalStateException("Replacing value of " + var0 + " with " + param1);
        } else {
            return this;
        }
    }

    public static Variant variant() {
        return new Variant();
    }

    public static Variant merge(Variant param0, Variant param1) {
        Variant var0 = new Variant();
        var0.values.putAll(param0.values);
        var0.values.putAll(param1.values);
        return var0;
    }

    public JsonElement get() {
        JsonObject var0 = new JsonObject();
        this.values.values().forEach(param1 -> param1.addToVariant(var0));
        return var0;
    }

    public static JsonElement convertList(List<Variant> param0) {
        if (param0.size() == 1) {
            return param0.get(0).get();
        } else {
            JsonArray var0 = new JsonArray();
            param0.forEach(param1 -> var0.add(param1.get()));
            return var0;
        }
    }
}

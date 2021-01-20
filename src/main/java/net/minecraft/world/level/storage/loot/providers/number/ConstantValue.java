package net.minecraft.world.level.storage.loot.providers.number;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.GsonAdapterFactory;
import net.minecraft.world.level.storage.loot.LootContext;

public final class ConstantValue implements NumberProvider {
    private final float value;

    private ConstantValue(float param0) {
        this.value = param0;
    }

    @Override
    public LootNumberProviderType getType() {
        return NumberProviders.CONSTANT;
    }

    @Override
    public float getFloat(LootContext param0) {
        return this.value;
    }

    public static ConstantValue exactly(float param0) {
        return new ConstantValue(param0);
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (param0 != null && this.getClass() == param0.getClass()) {
            return Float.compare(((ConstantValue)param0).value, this.value) == 0;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.value != 0.0F ? Float.floatToIntBits(this.value) : 0;
    }

    public static class InlineSerializer implements GsonAdapterFactory.InlineSerializer<ConstantValue> {
        public JsonElement serialize(ConstantValue param0, JsonSerializationContext param1) {
            return new JsonPrimitive(param0.value);
        }

        public ConstantValue deserialize(JsonElement param0, JsonDeserializationContext param1) {
            return new ConstantValue(GsonHelper.convertToFloat(param0, "value"));
        }
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<ConstantValue> {
        public void serialize(JsonObject param0, ConstantValue param1, JsonSerializationContext param2) {
            param0.addProperty("value", param1.value);
        }

        public ConstantValue deserialize(JsonObject param0, JsonDeserializationContext param1) {
            float var0 = GsonHelper.getAsFloat(param0, "value");
            return new ConstantValue(var0);
        }
    }
}

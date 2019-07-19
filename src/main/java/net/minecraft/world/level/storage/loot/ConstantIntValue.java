package net.minecraft.world.level.storage.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Random;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public final class ConstantIntValue implements RandomIntGenerator {
    private final int value;

    public ConstantIntValue(int param0) {
        this.value = param0;
    }

    @Override
    public int getInt(Random param0) {
        return this.value;
    }

    @Override
    public ResourceLocation getType() {
        return CONSTANT;
    }

    public static ConstantIntValue exactly(int param0) {
        return new ConstantIntValue(param0);
    }

    public static class Serializer implements JsonDeserializer<ConstantIntValue>, JsonSerializer<ConstantIntValue> {
        public ConstantIntValue deserialize(JsonElement param0, Type param1, JsonDeserializationContext param2) throws JsonParseException {
            return new ConstantIntValue(GsonHelper.convertToInt(param0, "value"));
        }

        public JsonElement serialize(ConstantIntValue param0, Type param1, JsonSerializationContext param2) {
            return new JsonPrimitive(param0.value);
        }
    }
}

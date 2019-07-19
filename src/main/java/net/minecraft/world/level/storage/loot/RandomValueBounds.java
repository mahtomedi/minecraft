package net.minecraft.world.level.storage.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Random;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;

public class RandomValueBounds implements RandomIntGenerator {
    private final float min;
    private final float max;

    public RandomValueBounds(float param0, float param1) {
        this.min = param0;
        this.max = param1;
    }

    public RandomValueBounds(float param0) {
        this.min = param0;
        this.max = param0;
    }

    public static RandomValueBounds between(float param0, float param1) {
        return new RandomValueBounds(param0, param1);
    }

    public float getMin() {
        return this.min;
    }

    public float getMax() {
        return this.max;
    }

    @Override
    public int getInt(Random param0) {
        return Mth.nextInt(param0, Mth.floor(this.min), Mth.floor(this.max));
    }

    public float getFloat(Random param0) {
        return Mth.nextFloat(param0, this.min, this.max);
    }

    public boolean matchesValue(int param0) {
        return (float)param0 <= this.max && (float)param0 >= this.min;
    }

    @Override
    public ResourceLocation getType() {
        return UNIFORM;
    }

    public static class Serializer implements JsonDeserializer<RandomValueBounds>, JsonSerializer<RandomValueBounds> {
        public RandomValueBounds deserialize(JsonElement param0, Type param1, JsonDeserializationContext param2) throws JsonParseException {
            if (GsonHelper.isNumberValue(param0)) {
                return new RandomValueBounds(GsonHelper.convertToFloat(param0, "value"));
            } else {
                JsonObject var0 = GsonHelper.convertToJsonObject(param0, "value");
                float var1 = GsonHelper.getAsFloat(var0, "min");
                float var2 = GsonHelper.getAsFloat(var0, "max");
                return new RandomValueBounds(var1, var2);
            }
        }

        public JsonElement serialize(RandomValueBounds param0, Type param1, JsonSerializationContext param2) {
            if (param0.min == param0.max) {
                return new JsonPrimitive(param0.min);
            } else {
                JsonObject var0 = new JsonObject();
                var0.addProperty("min", param0.min);
                var0.addProperty("max", param0.max);
                return var0;
            }
        }
    }
}

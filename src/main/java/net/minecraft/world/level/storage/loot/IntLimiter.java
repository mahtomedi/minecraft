package net.minecraft.world.level.storage.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.function.IntUnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;

public class IntLimiter implements IntUnaryOperator {
    private final Integer min;
    private final Integer max;
    private final IntUnaryOperator op;

    private IntLimiter(@Nullable Integer param0, @Nullable Integer param1) {
        this.min = param0;
        this.max = param1;
        if (param0 == null) {
            if (param1 == null) {
                this.op = param0x -> param0x;
            } else {
                int var0 = param1;
                this.op = param1x -> Math.min(var0, param1x);
            }
        } else {
            int var1 = param0;
            if (param1 == null) {
                this.op = param1x -> Math.max(var1, param1x);
            } else {
                int var2 = param1;
                this.op = param2 -> Mth.clamp(param2, var1, var2);
            }
        }

    }

    public static IntLimiter clamp(int param0, int param1) {
        return new IntLimiter(param0, param1);
    }

    public static IntLimiter lowerBound(int param0) {
        return new IntLimiter(param0, null);
    }

    public static IntLimiter upperBound(int param0) {
        return new IntLimiter(null, param0);
    }

    @Override
    public int applyAsInt(int param0) {
        return this.op.applyAsInt(param0);
    }

    public static class Serializer implements JsonDeserializer<IntLimiter>, JsonSerializer<IntLimiter> {
        public IntLimiter deserialize(JsonElement param0, Type param1, JsonDeserializationContext param2) throws JsonParseException {
            JsonObject var0 = GsonHelper.convertToJsonObject(param0, "value");
            Integer var1 = var0.has("min") ? GsonHelper.getAsInt(var0, "min") : null;
            Integer var2 = var0.has("max") ? GsonHelper.getAsInt(var0, "max") : null;
            return new IntLimiter(var1, var2);
        }

        public JsonElement serialize(IntLimiter param0, Type param1, JsonSerializationContext param2) {
            JsonObject var0 = new JsonObject();
            if (param0.max != null) {
                var0.addProperty("max", param0.max);
            }

            if (param0.min != null) {
                var0.addProperty("min", param0.min);
            }

            return var0;
        }
    }
}

package net.minecraft.world.level.storage.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Random;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public final class BinomialDistributionGenerator implements RandomIntGenerator {
    private final int n;
    private final float p;

    public BinomialDistributionGenerator(int param0, float param1) {
        this.n = param0;
        this.p = param1;
    }

    @Override
    public int getInt(Random param0) {
        int var0 = 0;

        for(int var1 = 0; var1 < this.n; ++var1) {
            if (param0.nextFloat() < this.p) {
                ++var0;
            }
        }

        return var0;
    }

    public static BinomialDistributionGenerator binomial(int param0, float param1) {
        return new BinomialDistributionGenerator(param0, param1);
    }

    @Override
    public ResourceLocation getType() {
        return BINOMIAL;
    }

    public static class Serializer implements JsonDeserializer<BinomialDistributionGenerator>, JsonSerializer<BinomialDistributionGenerator> {
        public BinomialDistributionGenerator deserialize(JsonElement param0, Type param1, JsonDeserializationContext param2) throws JsonParseException {
            JsonObject var0 = GsonHelper.convertToJsonObject(param0, "value");
            int var1 = GsonHelper.getAsInt(var0, "n");
            float var2 = GsonHelper.getAsFloat(var0, "p");
            return new BinomialDistributionGenerator(var1, var2);
        }

        public JsonElement serialize(BinomialDistributionGenerator param0, Type param1, JsonSerializationContext param2) {
            JsonObject var0 = new JsonObject();
            var0.addProperty("n", param0.n);
            var0.addProperty("p", param0.p);
            return var0;
        }
    }
}

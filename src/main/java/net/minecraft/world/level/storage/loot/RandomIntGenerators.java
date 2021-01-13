package net.minecraft.world.level.storage.loot;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class RandomIntGenerators {
    private static final Map<ResourceLocation, Class<? extends RandomIntGenerator>> GENERATORS = Maps.newHashMap();

    public static RandomIntGenerator deserialize(JsonElement param0, JsonDeserializationContext param1) throws JsonParseException {
        if (param0.isJsonPrimitive()) {
            return param1.deserialize(param0, ConstantIntValue.class);
        } else {
            JsonObject var0 = param0.getAsJsonObject();
            String var1 = GsonHelper.getAsString(var0, "type", RandomIntGenerator.UNIFORM.toString());
            Class<? extends RandomIntGenerator> var2 = GENERATORS.get(new ResourceLocation(var1));
            if (var2 == null) {
                throw new JsonParseException("Unknown generator: " + var1);
            } else {
                return param1.deserialize(var0, var2);
            }
        }
    }

    public static JsonElement serialize(RandomIntGenerator param0, JsonSerializationContext param1) {
        JsonElement var0 = param1.serialize(param0);
        if (var0.isJsonObject()) {
            var0.getAsJsonObject().addProperty("type", param0.getType().toString());
        }

        return var0;
    }

    static {
        GENERATORS.put(RandomIntGenerator.UNIFORM, RandomValueBounds.class);
        GENERATORS.put(RandomIntGenerator.BINOMIAL, BinomialDistributionGenerator.class);
        GENERATORS.put(RandomIntGenerator.CONSTANT, ConstantIntValue.class);
    }
}

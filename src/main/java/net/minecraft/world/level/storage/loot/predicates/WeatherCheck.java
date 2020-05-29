package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;

public class WeatherCheck implements LootItemCondition {
    @Nullable
    private final Boolean isRaining;
    @Nullable
    private final Boolean isThundering;

    private WeatherCheck(@Nullable Boolean param0, @Nullable Boolean param1) {
        this.isRaining = param0;
        this.isThundering = param1;
    }

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.WEATHER_CHECK;
    }

    public boolean test(LootContext param0) {
        ServerLevel var0 = param0.getLevel();
        if (this.isRaining != null && this.isRaining != var0.isRaining()) {
            return false;
        } else {
            return this.isThundering == null || this.isThundering == var0.isThundering();
        }
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<WeatherCheck> {
        public void serialize(JsonObject param0, WeatherCheck param1, JsonSerializationContext param2) {
            param0.addProperty("raining", param1.isRaining);
            param0.addProperty("thundering", param1.isThundering);
        }

        public WeatherCheck deserialize(JsonObject param0, JsonDeserializationContext param1) {
            Boolean var0 = param0.has("raining") ? GsonHelper.getAsBoolean(param0, "raining") : null;
            Boolean var1 = param0.has("thundering") ? GsonHelper.getAsBoolean(param0, "thundering") : null;
            return new WeatherCheck(var0, var1);
        }
    }
}

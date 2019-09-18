package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.RandomValueBounds;

public class TimeCheck implements LootItemCondition {
    @Nullable
    private final Long period;
    private final RandomValueBounds value;

    private TimeCheck(@Nullable Long param0, RandomValueBounds param1) {
        this.period = param0;
        this.value = param1;
    }

    public boolean test(LootContext param0) {
        ServerLevel var0 = param0.getLevel();
        long var1 = var0.getDayTime();
        if (this.period != null) {
            var1 %= this.period;
        }

        return this.value.matchesValue((int)var1);
    }

    public static class Serializer extends LootItemCondition.Serializer<TimeCheck> {
        public Serializer() {
            super(new ResourceLocation("time_check"), TimeCheck.class);
        }

        public void serialize(JsonObject param0, TimeCheck param1, JsonSerializationContext param2) {
            param0.addProperty("period", param1.period);
            param0.add("value", param2.serialize(param1.value));
        }

        public TimeCheck deserialize(JsonObject param0, JsonDeserializationContext param1) {
            Long var0 = param0.has("period") ? GsonHelper.getAsLong(param0, "period") : null;
            RandomValueBounds var1 = GsonHelper.getAsObject(param0, "value", param1, RandomValueBounds.class);
            return new TimeCheck(var0, var1);
        }
    }
}

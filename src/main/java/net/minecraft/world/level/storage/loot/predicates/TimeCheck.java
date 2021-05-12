package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public class TimeCheck implements LootItemCondition {
    @Nullable
    final Long period;
    final IntRange value;

    TimeCheck(@Nullable Long param0, IntRange param1) {
        this.period = param0;
        this.value = param1;
    }

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.TIME_CHECK;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.value.getReferencedContextParams();
    }

    public boolean test(LootContext param0) {
        ServerLevel var0 = param0.getLevel();
        long var1 = var0.getDayTime();
        if (this.period != null) {
            var1 %= this.period;
        }

        return this.value.test(param0, (int)var1);
    }

    public static TimeCheck.Builder time(IntRange param0) {
        return new TimeCheck.Builder(param0);
    }

    public static class Builder implements LootItemCondition.Builder {
        @Nullable
        private Long period;
        private final IntRange value;

        public Builder(IntRange param0) {
            this.value = param0;
        }

        public TimeCheck.Builder setPeriod(long param0) {
            this.period = param0;
            return this;
        }

        public TimeCheck build() {
            return new TimeCheck(this.period, this.value);
        }
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<TimeCheck> {
        public void serialize(JsonObject param0, TimeCheck param1, JsonSerializationContext param2) {
            param0.addProperty("period", param1.period);
            param0.add("value", param2.serialize(param1.value));
        }

        public TimeCheck deserialize(JsonObject param0, JsonDeserializationContext param1) {
            Long var0 = param0.has("period") ? GsonHelper.getAsLong(param0, "period") : null;
            IntRange var1 = GsonHelper.getAsObject(param0, "value", param1, IntRange.class);
            return new TimeCheck(var0, var1);
        }
    }
}

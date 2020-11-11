package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public class ValueCheckCondition implements LootItemCondition {
    private final NumberProvider provider;
    private final IntRange range;

    private ValueCheckCondition(NumberProvider param0, IntRange param1) {
        this.provider = param0;
        this.range = param1;
    }

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.VALUE_CHECK;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Sets.union(this.provider.getReferencedContextParams(), this.range.getReferencedContextParams());
    }

    public boolean test(LootContext param0) {
        return this.range.test(param0, this.provider.getInt(param0));
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<ValueCheckCondition> {
        public void serialize(JsonObject param0, ValueCheckCondition param1, JsonSerializationContext param2) {
            param0.add("value", param2.serialize(param1.provider));
            param0.add("range", param2.serialize(param1.range));
        }

        public ValueCheckCondition deserialize(JsonObject param0, JsonDeserializationContext param1) {
            NumberProvider var0 = GsonHelper.getAsObject(param0, "value", param1, NumberProvider.class);
            IntRange var1 = GsonHelper.getAsObject(param0, "range", param1, IntRange.class);
            return new ValueCheckCondition(var0, var1);
        }
    }
}

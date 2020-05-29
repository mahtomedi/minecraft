package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;

public class AlternativeLootItemCondition implements LootItemCondition {
    private final LootItemCondition[] terms;
    private final Predicate<LootContext> composedPredicate;

    private AlternativeLootItemCondition(LootItemCondition[] param0) {
        this.terms = param0;
        this.composedPredicate = LootItemConditions.orConditions(param0);
    }

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.ALTERNATIVE;
    }

    public final boolean test(LootContext param0) {
        return this.composedPredicate.test(param0);
    }

    @Override
    public void validate(ValidationContext param0) {
        LootItemCondition.super.validate(param0);

        for(int var0 = 0; var0 < this.terms.length; ++var0) {
            this.terms[var0].validate(param0.forChild(".term[" + var0 + "]"));
        }

    }

    public static AlternativeLootItemCondition.Builder alternative(LootItemCondition.Builder... param0) {
        return new AlternativeLootItemCondition.Builder(param0);
    }

    public static class Builder implements LootItemCondition.Builder {
        private final List<LootItemCondition> terms = Lists.newArrayList();

        public Builder(LootItemCondition.Builder... param0) {
            for(LootItemCondition.Builder var0 : param0) {
                this.terms.add(var0.build());
            }

        }

        @Override
        public AlternativeLootItemCondition.Builder or(LootItemCondition.Builder param0) {
            this.terms.add(param0.build());
            return this;
        }

        @Override
        public LootItemCondition build() {
            return new AlternativeLootItemCondition(this.terms.toArray(new LootItemCondition[0]));
        }
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<AlternativeLootItemCondition> {
        public void serialize(JsonObject param0, AlternativeLootItemCondition param1, JsonSerializationContext param2) {
            param0.add("terms", param2.serialize(param1.terms));
        }

        public AlternativeLootItemCondition deserialize(JsonObject param0, JsonDeserializationContext param1) {
            LootItemCondition[] var0 = GsonHelper.getAsObject(param0, "terms", param1, LootItemCondition[].class);
            return new AlternativeLootItemCondition(var0);
        }
    }
}

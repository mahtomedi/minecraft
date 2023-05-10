package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;

public abstract class CompositeLootItemCondition implements LootItemCondition {
    final LootItemCondition[] terms;
    private final Predicate<LootContext> composedPredicate;

    protected CompositeLootItemCondition(LootItemCondition[] param0, Predicate<LootContext> param1) {
        this.terms = param0;
        this.composedPredicate = param1;
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

    public abstract static class Builder implements LootItemCondition.Builder {
        private final List<LootItemCondition> terms = new ArrayList<>();

        public Builder(LootItemCondition.Builder... param0) {
            for(LootItemCondition.Builder var0 : param0) {
                this.terms.add(var0.build());
            }

        }

        public void addTerm(LootItemCondition.Builder param0) {
            this.terms.add(param0.build());
        }

        @Override
        public LootItemCondition build() {
            LootItemCondition[] var0 = this.terms.toArray(param0 -> new LootItemCondition[param0]);
            return this.create(var0);
        }

        protected abstract LootItemCondition create(LootItemCondition[] var1);
    }

    public abstract static class Serializer<T extends CompositeLootItemCondition> implements net.minecraft.world.level.storage.loot.Serializer<T> {
        public void serialize(JsonObject param0, CompositeLootItemCondition param1, JsonSerializationContext param2) {
            param0.add("terms", param2.serialize(param1.terms));
        }

        public T deserialize(JsonObject param0, JsonDeserializationContext param1) {
            LootItemCondition[] var0 = GsonHelper.getAsObject(param0, "terms", param1, LootItemCondition[].class);
            return this.create(var0);
        }

        protected abstract T create(LootItemCondition[] var1);
    }
}

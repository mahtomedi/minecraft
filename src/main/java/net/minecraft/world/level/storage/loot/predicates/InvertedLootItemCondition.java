package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public class InvertedLootItemCondition implements LootItemCondition {
    final LootItemCondition term;

    InvertedLootItemCondition(LootItemCondition param0) {
        this.term = param0;
    }

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.INVERTED;
    }

    public final boolean test(LootContext param0) {
        return !this.term.test(param0);
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.term.getReferencedContextParams();
    }

    @Override
    public void validate(ValidationContext param0) {
        LootItemCondition.super.validate(param0);
        this.term.validate(param0);
    }

    public static LootItemCondition.Builder invert(LootItemCondition.Builder param0) {
        InvertedLootItemCondition var0 = new InvertedLootItemCondition(param0.build());
        return () -> var0;
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<InvertedLootItemCondition> {
        public void serialize(JsonObject param0, InvertedLootItemCondition param1, JsonSerializationContext param2) {
            param0.add("term", param2.serialize(param1.term));
        }

        public InvertedLootItemCondition deserialize(JsonObject param0, JsonDeserializationContext param1) {
            LootItemCondition var0 = GsonHelper.getAsObject(param0, "term", param1, LootItemCondition.class);
            return new InvertedLootItemCondition(var0);
        }
    }
}

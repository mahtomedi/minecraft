package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;

public class LootItemRandomChanceCondition implements LootItemCondition {
    final float probability;

    LootItemRandomChanceCondition(float param0) {
        this.probability = param0;
    }

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.RANDOM_CHANCE;
    }

    public boolean test(LootContext param0) {
        return param0.getRandom().nextFloat() < this.probability;
    }

    public static LootItemCondition.Builder randomChance(float param0) {
        return () -> new LootItemRandomChanceCondition(param0);
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<LootItemRandomChanceCondition> {
        public void serialize(JsonObject param0, LootItemRandomChanceCondition param1, JsonSerializationContext param2) {
            param0.addProperty("chance", param1.probability);
        }

        public LootItemRandomChanceCondition deserialize(JsonObject param0, JsonDeserializationContext param1) {
            return new LootItemRandomChanceCondition(GsonHelper.getAsFloat(param0, "chance"));
        }
    }
}

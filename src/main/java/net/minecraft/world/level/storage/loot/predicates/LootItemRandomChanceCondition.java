package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;

public class LootItemRandomChanceCondition implements LootItemCondition {
    private final float probability;

    private LootItemRandomChanceCondition(float param0) {
        this.probability = param0;
    }

    public boolean test(LootContext param0) {
        return param0.getRandom().nextFloat() < this.probability;
    }

    public static LootItemCondition.Builder randomChance(float param0) {
        return () -> new LootItemRandomChanceCondition(param0);
    }

    public static class Serializer extends LootItemCondition.Serializer<LootItemRandomChanceCondition> {
        protected Serializer() {
            super(new ResourceLocation("random_chance"), LootItemRandomChanceCondition.class);
        }

        public void serialize(JsonObject param0, LootItemRandomChanceCondition param1, JsonSerializationContext param2) {
            param0.addProperty("chance", param1.probability);
        }

        public LootItemRandomChanceCondition deserialize(JsonObject param0, JsonDeserializationContext param1) {
            return new LootItemRandomChanceCondition(GsonHelper.getAsFloat(param0, "chance"));
        }
    }
}

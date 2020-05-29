package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class LootItemRandomChanceWithLootingCondition implements LootItemCondition {
    private final float percent;
    private final float lootingMultiplier;

    private LootItemRandomChanceWithLootingCondition(float param0, float param1) {
        this.percent = param0;
        this.lootingMultiplier = param1;
    }

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.RANDOM_CHANCE_WITH_LOOTING;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.KILLER_ENTITY);
    }

    public boolean test(LootContext param0) {
        Entity var0 = param0.getParamOrNull(LootContextParams.KILLER_ENTITY);
        int var1 = 0;
        if (var0 instanceof LivingEntity) {
            var1 = EnchantmentHelper.getMobLooting((LivingEntity)var0);
        }

        return param0.getRandom().nextFloat() < this.percent + (float)var1 * this.lootingMultiplier;
    }

    public static LootItemCondition.Builder randomChanceAndLootingBoost(float param0, float param1) {
        return () -> new LootItemRandomChanceWithLootingCondition(param0, param1);
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<LootItemRandomChanceWithLootingCondition> {
        public void serialize(JsonObject param0, LootItemRandomChanceWithLootingCondition param1, JsonSerializationContext param2) {
            param0.addProperty("chance", param1.percent);
            param0.addProperty("looting_multiplier", param1.lootingMultiplier);
        }

        public LootItemRandomChanceWithLootingCondition deserialize(JsonObject param0, JsonDeserializationContext param1) {
            return new LootItemRandomChanceWithLootingCondition(GsonHelper.getAsFloat(param0, "chance"), GsonHelper.getAsFloat(param0, "looting_multiplier"));
        }
    }
}

package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class MatchTool implements LootItemCondition {
    final ItemPredicate predicate;

    public MatchTool(ItemPredicate param0) {
        this.predicate = param0;
    }

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.MATCH_TOOL;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.TOOL);
    }

    public boolean test(LootContext param0) {
        ItemStack var0 = param0.getParamOrNull(LootContextParams.TOOL);
        return var0 != null && this.predicate.matches(var0);
    }

    public static LootItemCondition.Builder toolMatches(ItemPredicate.Builder param0) {
        return () -> new MatchTool(param0.build());
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<MatchTool> {
        public void serialize(JsonObject param0, MatchTool param1, JsonSerializationContext param2) {
            param0.add("predicate", param1.predicate.serializeToJson());
        }

        public MatchTool deserialize(JsonObject param0, JsonDeserializationContext param1) {
            ItemPredicate var0 = ItemPredicate.fromJson(param0.get("predicate"));
            return new MatchTool(var0);
        }
    }
}

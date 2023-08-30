package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.Arrays;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;

public class ItemUsedOnLocationTrigger extends SimpleCriterionTrigger<ItemUsedOnLocationTrigger.TriggerInstance> {
    public ItemUsedOnLocationTrigger.TriggerInstance createInstance(JsonObject param0, Optional<ContextAwarePredicate> param1, DeserializationContext param2) {
        Optional<Optional<ContextAwarePredicate>> var0 = ContextAwarePredicate.fromElement(
            "location", param2, param0.get("location"), LootContextParamSets.ADVANCEMENT_LOCATION
        );
        if (var0.isEmpty()) {
            throw new JsonParseException("Failed to parse 'location' field");
        } else {
            return new ItemUsedOnLocationTrigger.TriggerInstance(param1, var0.get());
        }
    }

    public void trigger(ServerPlayer param0, BlockPos param1, ItemStack param2) {
        ServerLevel var0 = param0.serverLevel();
        BlockState var1 = var0.getBlockState(param1);
        LootParams var2 = new LootParams.Builder(var0)
            .withParameter(LootContextParams.ORIGIN, param1.getCenter())
            .withParameter(LootContextParams.THIS_ENTITY, param0)
            .withParameter(LootContextParams.BLOCK_STATE, var1)
            .withParameter(LootContextParams.TOOL, param2)
            .create(LootContextParamSets.ADVANCEMENT_LOCATION);
        LootContext var3 = new LootContext.Builder(var2).create(Optional.empty());
        this.trigger(param0, param1x -> param1x.matches(var3));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Optional<ContextAwarePredicate> location;

        public TriggerInstance(Optional<ContextAwarePredicate> param0, Optional<ContextAwarePredicate> param1) {
            super(param0);
            this.location = param1;
        }

        public static Criterion<ItemUsedOnLocationTrigger.TriggerInstance> placedBlock(Block param0) {
            ContextAwarePredicate var0 = ContextAwarePredicate.create(LootItemBlockStatePropertyCondition.hasBlockStateProperties(param0).build());
            return CriteriaTriggers.PLACED_BLOCK.createCriterion(new ItemUsedOnLocationTrigger.TriggerInstance(Optional.empty(), Optional.of(var0)));
        }

        public static Criterion<ItemUsedOnLocationTrigger.TriggerInstance> placedBlock(LootItemCondition.Builder... param0) {
            ContextAwarePredicate var0 = ContextAwarePredicate.create(
                Arrays.stream(param0).map(LootItemCondition.Builder::build).toArray(param0x -> new LootItemCondition[param0x])
            );
            return CriteriaTriggers.PLACED_BLOCK.createCriterion(new ItemUsedOnLocationTrigger.TriggerInstance(Optional.empty(), Optional.of(var0)));
        }

        private static ItemUsedOnLocationTrigger.TriggerInstance itemUsedOnLocation(LocationPredicate.Builder param0, ItemPredicate.Builder param1) {
            ContextAwarePredicate var0 = ContextAwarePredicate.create(LocationCheck.checkLocation(param0).build(), MatchTool.toolMatches(param1).build());
            return new ItemUsedOnLocationTrigger.TriggerInstance(Optional.empty(), Optional.of(var0));
        }

        public static Criterion<ItemUsedOnLocationTrigger.TriggerInstance> itemUsedOnBlock(LocationPredicate.Builder param0, ItemPredicate.Builder param1) {
            return CriteriaTriggers.ITEM_USED_ON_BLOCK.createCriterion(itemUsedOnLocation(param0, param1));
        }

        public static Criterion<ItemUsedOnLocationTrigger.TriggerInstance> allayDropItemOnBlock(LocationPredicate.Builder param0, ItemPredicate.Builder param1) {
            return CriteriaTriggers.ALLAY_DROP_ITEM_ON_BLOCK.createCriterion(itemUsedOnLocation(param0, param1));
        }

        public boolean matches(LootContext param0) {
            return this.location.isEmpty() || this.location.get().matches(param0);
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject var0 = super.serializeToJson();
            this.location.ifPresent(param1 -> var0.add("location", param1.toJson()));
            return var0;
        }
    }
}

package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.Arrays;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
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
    final ResourceLocation id;

    public ItemUsedOnLocationTrigger(ResourceLocation param0) {
        this.id = param0;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    public ItemUsedOnLocationTrigger.TriggerInstance createInstance(JsonObject param0, Optional<ContextAwarePredicate> param1, DeserializationContext param2) {
        Optional<Optional<ContextAwarePredicate>> var0 = ContextAwarePredicate.fromElement(
            "location", param2, param0.get("location"), LootContextParamSets.ADVANCEMENT_LOCATION
        );
        if (var0.isEmpty()) {
            throw new JsonParseException("Failed to parse 'location' field");
        } else {
            return new ItemUsedOnLocationTrigger.TriggerInstance(this.id, param1, var0.get());
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

        public TriggerInstance(ResourceLocation param0, Optional<ContextAwarePredicate> param1, Optional<ContextAwarePredicate> param2) {
            super(param0, param1);
            this.location = param2;
        }

        public static ItemUsedOnLocationTrigger.TriggerInstance placedBlock(Block param0) {
            ContextAwarePredicate var0 = ContextAwarePredicate.create(LootItemBlockStatePropertyCondition.hasBlockStateProperties(param0).build());
            return new ItemUsedOnLocationTrigger.TriggerInstance(CriteriaTriggers.PLACED_BLOCK.id, Optional.empty(), Optional.of(var0));
        }

        public static ItemUsedOnLocationTrigger.TriggerInstance placedBlock(LootItemCondition.Builder... param0) {
            ContextAwarePredicate var0 = ContextAwarePredicate.create(
                Arrays.stream(param0).map(LootItemCondition.Builder::build).toArray(param0x -> new LootItemCondition[param0x])
            );
            return new ItemUsedOnLocationTrigger.TriggerInstance(CriteriaTriggers.PLACED_BLOCK.id, Optional.empty(), Optional.of(var0));
        }

        private static ItemUsedOnLocationTrigger.TriggerInstance itemUsedOnLocation(
            LocationPredicate.Builder param0, ItemPredicate.Builder param1, ResourceLocation param2
        ) {
            ContextAwarePredicate var0 = ContextAwarePredicate.create(LocationCheck.checkLocation(param0).build(), MatchTool.toolMatches(param1).build());
            return new ItemUsedOnLocationTrigger.TriggerInstance(param2, Optional.empty(), Optional.of(var0));
        }

        public static ItemUsedOnLocationTrigger.TriggerInstance itemUsedOnBlock(LocationPredicate.Builder param0, ItemPredicate.Builder param1) {
            return itemUsedOnLocation(param0, param1, CriteriaTriggers.ITEM_USED_ON_BLOCK.id);
        }

        public static ItemUsedOnLocationTrigger.TriggerInstance allayDropItemOnBlock(LocationPredicate.Builder param0, ItemPredicate.Builder param1) {
            return itemUsedOnLocation(param0, param1, CriteriaTriggers.ALLAY_DROP_ITEM_ON_BLOCK.id);
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

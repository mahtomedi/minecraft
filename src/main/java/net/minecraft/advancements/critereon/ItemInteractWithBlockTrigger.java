package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class ItemInteractWithBlockTrigger extends SimpleCriterionTrigger<ItemInteractWithBlockTrigger.TriggerInstance> {
    final ResourceLocation id;

    public ItemInteractWithBlockTrigger(ResourceLocation param0) {
        this.id = param0;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    public ItemInteractWithBlockTrigger.TriggerInstance createInstance(JsonObject param0, EntityPredicate.Composite param1, DeserializationContext param2) {
        LocationPredicate var0 = LocationPredicate.fromJson(param0.get("location"));
        ItemPredicate var1 = ItemPredicate.fromJson(param0.get("item"));
        return new ItemInteractWithBlockTrigger.TriggerInstance(this.id, param1, var0, var1);
    }

    public void trigger(ServerPlayer param0, BlockPos param1, ItemStack param2) {
        BlockState var0 = param0.getLevel().getBlockState(param1);
        this.trigger(param0, param4 -> param4.matches(var0, param0.getLevel(), param1, param2));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final LocationPredicate location;
        private final ItemPredicate item;

        public TriggerInstance(ResourceLocation param0, EntityPredicate.Composite param1, LocationPredicate param2, ItemPredicate param3) {
            super(param0, param1);
            this.location = param2;
            this.item = param3;
        }

        public static ItemInteractWithBlockTrigger.TriggerInstance itemUsedOnBlock(LocationPredicate.Builder param0, ItemPredicate.Builder param1) {
            return new ItemInteractWithBlockTrigger.TriggerInstance(
                CriteriaTriggers.ITEM_USED_ON_BLOCK.id, EntityPredicate.Composite.ANY, param0.build(), param1.build()
            );
        }

        public static ItemInteractWithBlockTrigger.TriggerInstance allayDropItemOnBlock(LocationPredicate.Builder param0, ItemPredicate.Builder param1) {
            return new ItemInteractWithBlockTrigger.TriggerInstance(
                CriteriaTriggers.ALLAY_DROP_ITEM_ON_BLOCK.id, EntityPredicate.Composite.ANY, param0.build(), param1.build()
            );
        }

        public boolean matches(BlockState param0, ServerLevel param1, BlockPos param2, ItemStack param3) {
            return !this.location.matches(param1, (double)param2.getX() + 0.5, (double)param2.getY() + 0.5, (double)param2.getZ() + 0.5)
                ? false
                : this.item.matches(param3);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext param0) {
            JsonObject var0 = super.serializeToJson(param0);
            var0.add("location", this.location.serializeToJson());
            var0.add("item", this.item.serializeToJson());
            return var0;
        }
    }
}

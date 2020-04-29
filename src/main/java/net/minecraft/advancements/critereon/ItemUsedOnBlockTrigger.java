package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class ItemUsedOnBlockTrigger extends SimpleCriterionTrigger<ItemUsedOnBlockTrigger.TriggerInstance> {
    private final ResourceLocation id;

    public ItemUsedOnBlockTrigger(ResourceLocation param0) {
        this.id = param0;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    public ItemUsedOnBlockTrigger.TriggerInstance createInstance(JsonObject param0, EntityPredicate.Composite param1, DeserializationContext param2) {
        BlockPredicate var0 = BlockPredicate.fromJson(param0.get("block"));
        StatePropertiesPredicate var1 = StatePropertiesPredicate.fromJson(param0.get("state"));
        ItemPredicate var2 = ItemPredicate.fromJson(param0.get("item"));
        return new ItemUsedOnBlockTrigger.TriggerInstance(this.id, param1, var0, var1, var2);
    }

    public void trigger(ServerPlayer param0, BlockPos param1, ItemStack param2) {
        BlockState var0 = param0.getLevel().getBlockState(param1);
        this.trigger(param0, param4 -> param4.matches(var0, param0.getLevel(), param1, param2));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final BlockPredicate block;
        private final StatePropertiesPredicate state;
        private final ItemPredicate item;

        public TriggerInstance(
            ResourceLocation param0, EntityPredicate.Composite param1, BlockPredicate param2, StatePropertiesPredicate param3, ItemPredicate param4
        ) {
            super(param0, param1);
            this.block = param2;
            this.state = param3;
            this.item = param4;
        }

        public static ItemUsedOnBlockTrigger.TriggerInstance safelyHarvestedHoney(BlockPredicate.Builder param0, ItemPredicate.Builder param1) {
            return new ItemUsedOnBlockTrigger.TriggerInstance(
                CriteriaTriggers.SAFELY_HARVEST_HONEY.id, EntityPredicate.Composite.ANY, param0.build(), StatePropertiesPredicate.ANY, param1.build()
            );
        }

        public boolean matches(BlockState param0, ServerLevel param1, BlockPos param2, ItemStack param3) {
            if (!this.block.matches(param1, param2)) {
                return false;
            } else if (!this.state.matches(param0)) {
                return false;
            } else {
                return this.item.matches(param3);
            }
        }

        @Override
        public JsonObject serializeToJson(SerializationContext param0) {
            JsonObject var0 = super.serializeToJson(param0);
            var0.add("block", this.block.serializeToJson());
            var0.add("state", this.state.serializeToJson());
            var0.add("item", this.item.serializeToJson());
            return var0;
        }
    }
}

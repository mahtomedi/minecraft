package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class EnchantedItemTrigger extends SimpleCriterionTrigger<EnchantedItemTrigger.TriggerInstance> {
    static final ResourceLocation ID = new ResourceLocation("enchanted_item");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public EnchantedItemTrigger.TriggerInstance createInstance(JsonObject param0, ContextAwarePredicate param1, DeserializationContext param2) {
        ItemPredicate var0 = ItemPredicate.fromJson(param0.get("item"));
        MinMaxBounds.Ints var1 = MinMaxBounds.Ints.fromJson(param0.get("levels"));
        return new EnchantedItemTrigger.TriggerInstance(param1, var0, var1);
    }

    public void trigger(ServerPlayer param0, ItemStack param1, int param2) {
        this.trigger(param0, param2x -> param2x.matches(param1, param2));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final ItemPredicate item;
        private final MinMaxBounds.Ints levels;

        public TriggerInstance(ContextAwarePredicate param0, ItemPredicate param1, MinMaxBounds.Ints param2) {
            super(EnchantedItemTrigger.ID, param0);
            this.item = param1;
            this.levels = param2;
        }

        public static EnchantedItemTrigger.TriggerInstance enchantedItem() {
            return new EnchantedItemTrigger.TriggerInstance(ContextAwarePredicate.ANY, ItemPredicate.ANY, MinMaxBounds.Ints.ANY);
        }

        public boolean matches(ItemStack param0, int param1) {
            if (!this.item.matches(param0)) {
                return false;
            } else {
                return this.levels.matches(param1);
            }
        }

        @Override
        public JsonObject serializeToJson(SerializationContext param0) {
            JsonObject var0 = super.serializeToJson(param0);
            var0.add("item", this.item.serializeToJson());
            var0.add("levels", this.levels.serializeToJson());
            return var0;
        }
    }
}

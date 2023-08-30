package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ItemDurabilityTrigger extends SimpleCriterionTrigger<ItemDurabilityTrigger.TriggerInstance> {
    public ItemDurabilityTrigger.TriggerInstance createInstance(JsonObject param0, Optional<ContextAwarePredicate> param1, DeserializationContext param2) {
        Optional<ItemPredicate> var0 = ItemPredicate.fromJson(param0.get("item"));
        MinMaxBounds.Ints var1 = MinMaxBounds.Ints.fromJson(param0.get("durability"));
        MinMaxBounds.Ints var2 = MinMaxBounds.Ints.fromJson(param0.get("delta"));
        return new ItemDurabilityTrigger.TriggerInstance(param1, var0, var1, var2);
    }

    public void trigger(ServerPlayer param0, ItemStack param1, int param2) {
        this.trigger(param0, param2x -> param2x.matches(param1, param2));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Optional<ItemPredicate> item;
        private final MinMaxBounds.Ints durability;
        private final MinMaxBounds.Ints delta;

        public TriggerInstance(Optional<ContextAwarePredicate> param0, Optional<ItemPredicate> param1, MinMaxBounds.Ints param2, MinMaxBounds.Ints param3) {
            super(param0);
            this.item = param1;
            this.durability = param2;
            this.delta = param3;
        }

        public static Criterion<ItemDurabilityTrigger.TriggerInstance> changedDurability(Optional<ItemPredicate> param0, MinMaxBounds.Ints param1) {
            return changedDurability(Optional.empty(), param0, param1);
        }

        public static Criterion<ItemDurabilityTrigger.TriggerInstance> changedDurability(
            Optional<ContextAwarePredicate> param0, Optional<ItemPredicate> param1, MinMaxBounds.Ints param2
        ) {
            return CriteriaTriggers.ITEM_DURABILITY_CHANGED
                .createCriterion(new ItemDurabilityTrigger.TriggerInstance(param0, param1, param2, MinMaxBounds.Ints.ANY));
        }

        public boolean matches(ItemStack param0, int param1) {
            if (this.item.isPresent() && !this.item.get().matches(param0)) {
                return false;
            } else if (!this.durability.matches(param0.getMaxDamage() - param1)) {
                return false;
            } else {
                return this.delta.matches(param0.getDamageValue() - param1);
            }
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject var0 = super.serializeToJson();
            this.item.ifPresent(param1 -> var0.add("item", param1.serializeToJson()));
            var0.add("durability", this.durability.serializeToJson());
            var0.add("delta", this.delta.serializeToJson());
            return var0;
        }
    }
}

package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class InventoryChangeTrigger extends SimpleCriterionTrigger<InventoryChangeTrigger.TriggerInstance> {
    public InventoryChangeTrigger.TriggerInstance createInstance(JsonObject param0, Optional<ContextAwarePredicate> param1, DeserializationContext param2) {
        JsonObject var0 = GsonHelper.getAsJsonObject(param0, "slots", new JsonObject());
        MinMaxBounds.Ints var1 = MinMaxBounds.Ints.fromJson(var0.get("occupied"));
        MinMaxBounds.Ints var2 = MinMaxBounds.Ints.fromJson(var0.get("full"));
        MinMaxBounds.Ints var3 = MinMaxBounds.Ints.fromJson(var0.get("empty"));
        List<ItemPredicate> var4 = ItemPredicate.fromJsonArray(param0.get("items"));
        return new InventoryChangeTrigger.TriggerInstance(param1, var1, var2, var3, var4);
    }

    public void trigger(ServerPlayer param0, Inventory param1, ItemStack param2) {
        int var0 = 0;
        int var1 = 0;
        int var2 = 0;

        for(int var3 = 0; var3 < param1.getContainerSize(); ++var3) {
            ItemStack var4 = param1.getItem(var3);
            if (var4.isEmpty()) {
                ++var1;
            } else {
                ++var2;
                if (var4.getCount() >= var4.getMaxStackSize()) {
                    ++var0;
                }
            }
        }

        this.trigger(param0, param1, param2, var0, var1, var2);
    }

    private void trigger(ServerPlayer param0, Inventory param1, ItemStack param2, int param3, int param4, int param5) {
        this.trigger(param0, param5x -> param5x.matches(param1, param2, param3, param4, param5));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final MinMaxBounds.Ints slotsOccupied;
        private final MinMaxBounds.Ints slotsFull;
        private final MinMaxBounds.Ints slotsEmpty;
        private final List<ItemPredicate> predicates;

        public TriggerInstance(
            Optional<ContextAwarePredicate> param0, MinMaxBounds.Ints param1, MinMaxBounds.Ints param2, MinMaxBounds.Ints param3, List<ItemPredicate> param4
        ) {
            super(param0);
            this.slotsOccupied = param1;
            this.slotsFull = param2;
            this.slotsEmpty = param3;
            this.predicates = param4;
        }

        public static Criterion<InventoryChangeTrigger.TriggerInstance> hasItems(ItemPredicate.Builder... param0) {
            return hasItems(Stream.of(param0).map(ItemPredicate.Builder::build).toArray(param0x -> new ItemPredicate[param0x]));
        }

        public static Criterion<InventoryChangeTrigger.TriggerInstance> hasItems(ItemPredicate... param0) {
            return CriteriaTriggers.INVENTORY_CHANGED
                .createCriterion(
                    new InventoryChangeTrigger.TriggerInstance(
                        Optional.empty(), MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, List.of(param0)
                    )
                );
        }

        public static Criterion<InventoryChangeTrigger.TriggerInstance> hasItems(ItemLike... param0) {
            ItemPredicate[] var0 = new ItemPredicate[param0.length];

            for(int var1 = 0; var1 < param0.length; ++var1) {
                var0[var1] = new ItemPredicate(
                    Optional.empty(),
                    Optional.of(HolderSet.direct(param0[var1].asItem().builtInRegistryHolder())),
                    MinMaxBounds.Ints.ANY,
                    MinMaxBounds.Ints.ANY,
                    List.of(),
                    List.of(),
                    Optional.empty(),
                    Optional.empty()
                );
            }

            return hasItems(var0);
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject var0 = super.serializeToJson();
            if (!this.slotsOccupied.isAny() || !this.slotsFull.isAny() || !this.slotsEmpty.isAny()) {
                JsonObject var1 = new JsonObject();
                var1.add("occupied", this.slotsOccupied.serializeToJson());
                var1.add("full", this.slotsFull.serializeToJson());
                var1.add("empty", this.slotsEmpty.serializeToJson());
                var0.add("slots", var1);
            }

            if (!this.predicates.isEmpty()) {
                var0.add("items", ItemPredicate.serializeToJsonArray(this.predicates));
            }

            return var0;
        }

        public boolean matches(Inventory param0, ItemStack param1, int param2, int param3, int param4) {
            if (!this.slotsFull.matches(param2)) {
                return false;
            } else if (!this.slotsEmpty.matches(param3)) {
                return false;
            } else if (!this.slotsOccupied.matches(param4)) {
                return false;
            } else if (this.predicates.isEmpty()) {
                return true;
            } else if (this.predicates.size() != 1) {
                List<ItemPredicate> var0 = new ObjectArrayList<>(this.predicates);
                int var1 = param0.getContainerSize();

                for(int var2 = 0; var2 < var1; ++var2) {
                    if (var0.isEmpty()) {
                        return true;
                    }

                    ItemStack var3 = param0.getItem(var2);
                    if (!var3.isEmpty()) {
                        var0.removeIf(param1x -> param1x.matches(var3));
                    }
                }

                return var0.isEmpty();
            } else {
                return !param1.isEmpty() && this.predicates.get(0).matches(param1);
            }
        }
    }
}

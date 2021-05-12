package net.minecraft.advancements.critereon;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class InventoryChangeTrigger extends SimpleCriterionTrigger<InventoryChangeTrigger.TriggerInstance> {
    static final ResourceLocation ID = new ResourceLocation("inventory_changed");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public InventoryChangeTrigger.TriggerInstance createInstance(JsonObject param0, EntityPredicate.Composite param1, DeserializationContext param2) {
        JsonObject var0 = GsonHelper.getAsJsonObject(param0, "slots", new JsonObject());
        MinMaxBounds.Ints var1 = MinMaxBounds.Ints.fromJson(var0.get("occupied"));
        MinMaxBounds.Ints var2 = MinMaxBounds.Ints.fromJson(var0.get("full"));
        MinMaxBounds.Ints var3 = MinMaxBounds.Ints.fromJson(var0.get("empty"));
        ItemPredicate[] var4 = ItemPredicate.fromJsonArray(param0.get("items"));
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
        private final ItemPredicate[] predicates;

        public TriggerInstance(
            EntityPredicate.Composite param0, MinMaxBounds.Ints param1, MinMaxBounds.Ints param2, MinMaxBounds.Ints param3, ItemPredicate[] param4
        ) {
            super(InventoryChangeTrigger.ID, param0);
            this.slotsOccupied = param1;
            this.slotsFull = param2;
            this.slotsEmpty = param3;
            this.predicates = param4;
        }

        public static InventoryChangeTrigger.TriggerInstance hasItems(ItemPredicate... param0) {
            return new InventoryChangeTrigger.TriggerInstance(
                EntityPredicate.Composite.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, param0
            );
        }

        public static InventoryChangeTrigger.TriggerInstance hasItems(ItemLike... param0) {
            ItemPredicate[] var0 = new ItemPredicate[param0.length];

            for(int var1 = 0; var1 < param0.length; ++var1) {
                var0[var1] = new ItemPredicate(
                    null,
                    param0[var1].asItem(),
                    MinMaxBounds.Ints.ANY,
                    MinMaxBounds.Ints.ANY,
                    EnchantmentPredicate.NONE,
                    EnchantmentPredicate.NONE,
                    null,
                    NbtPredicate.ANY
                );
            }

            return hasItems(var0);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext param0) {
            JsonObject var0 = super.serializeToJson(param0);
            if (!this.slotsOccupied.isAny() || !this.slotsFull.isAny() || !this.slotsEmpty.isAny()) {
                JsonObject var1 = new JsonObject();
                var1.add("occupied", this.slotsOccupied.serializeToJson());
                var1.add("full", this.slotsFull.serializeToJson());
                var1.add("empty", this.slotsEmpty.serializeToJson());
                var0.add("slots", var1);
            }

            if (this.predicates.length > 0) {
                JsonArray var2 = new JsonArray();

                for(ItemPredicate var3 : this.predicates) {
                    var2.add(var3.serializeToJson());
                }

                var0.add("items", var2);
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
            } else {
                int var0 = this.predicates.length;
                if (var0 == 0) {
                    return true;
                } else if (var0 != 1) {
                    List<ItemPredicate> var1 = new ObjectArrayList<>(this.predicates);
                    int var2 = param0.getContainerSize();

                    for(int var3 = 0; var3 < var2; ++var3) {
                        if (var1.isEmpty()) {
                            return true;
                        }

                        ItemStack var4 = param0.getItem(var3);
                        if (!var4.isEmpty()) {
                            var1.removeIf(param1x -> param1x.matches(var4));
                        }
                    }

                    return var1.isEmpty();
                } else {
                    return !param1.isEmpty() && this.predicates[0].matches(param1);
                }
            }
        }
    }
}

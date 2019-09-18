package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Iterator;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class InventoryChangeTrigger extends SimpleCriterionTrigger<InventoryChangeTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("inventory_changed");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public InventoryChangeTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        JsonObject var0 = GsonHelper.getAsJsonObject(param0, "slots", new JsonObject());
        MinMaxBounds.Ints var1 = MinMaxBounds.Ints.fromJson(var0.get("occupied"));
        MinMaxBounds.Ints var2 = MinMaxBounds.Ints.fromJson(var0.get("full"));
        MinMaxBounds.Ints var3 = MinMaxBounds.Ints.fromJson(var0.get("empty"));
        ItemPredicate[] var4 = ItemPredicate.fromJsonArray(param0.get("items"));
        return new InventoryChangeTrigger.TriggerInstance(var1, var2, var3, var4);
    }

    public void trigger(ServerPlayer param0, Inventory param1) {
        this.trigger(param0.getAdvancements(), param1x -> param1x.matches(param1));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final MinMaxBounds.Ints slotsOccupied;
        private final MinMaxBounds.Ints slotsFull;
        private final MinMaxBounds.Ints slotsEmpty;
        private final ItemPredicate[] predicates;

        public TriggerInstance(MinMaxBounds.Ints param0, MinMaxBounds.Ints param1, MinMaxBounds.Ints param2, ItemPredicate[] param3) {
            super(InventoryChangeTrigger.ID);
            this.slotsOccupied = param0;
            this.slotsFull = param1;
            this.slotsEmpty = param2;
            this.predicates = param3;
        }

        public static InventoryChangeTrigger.TriggerInstance hasItem(ItemPredicate... param0) {
            return new InventoryChangeTrigger.TriggerInstance(MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, param0);
        }

        public static InventoryChangeTrigger.TriggerInstance hasItem(ItemLike... param0) {
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

            return hasItem(var0);
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject var0 = new JsonObject();
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

        public boolean matches(Inventory param0) {
            int var0 = 0;
            int var1 = 0;
            int var2 = 0;
            List<ItemPredicate> var3 = Lists.newArrayList(this.predicates);

            for(int var4 = 0; var4 < param0.getContainerSize(); ++var4) {
                ItemStack var5 = param0.getItem(var4);
                if (var5.isEmpty()) {
                    ++var1;
                } else {
                    ++var2;
                    if (var5.getCount() >= var5.getMaxStackSize()) {
                        ++var0;
                    }

                    Iterator<ItemPredicate> var6 = var3.iterator();

                    while(var6.hasNext()) {
                        ItemPredicate var7 = var6.next();
                        if (var7.matches(var5)) {
                            var6.remove();
                        }
                    }
                }
            }

            if (!this.slotsFull.matches(var0)) {
                return false;
            } else if (!this.slotsEmpty.matches(var1)) {
                return false;
            } else if (!this.slotsOccupied.matches(var2)) {
                return false;
            } else {
                return var3.isEmpty();
            }
        }
    }
}

package net.minecraft.world.entity.npc;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public interface InventoryCarrier {
    String TAG_INVENTORY = "Inventory";

    SimpleContainer getInventory();

    static void pickUpItem(Mob param0, InventoryCarrier param1, ItemEntity param2) {
        ItemStack var0 = param2.getItem();
        if (param0.wantsToPickUp(var0)) {
            SimpleContainer var1 = param1.getInventory();
            boolean var2 = var1.canAddItem(var0);
            if (!var2) {
                return;
            }

            param0.onItemPickup(param2);
            int var3 = var0.getCount();
            ItemStack var4 = var1.addItem(var0);
            param0.take(param2, var3 - var4.getCount());
            if (var4.isEmpty()) {
                param2.discard();
            } else {
                var0.setCount(var4.getCount());
            }
        }

    }

    default void readInventoryFromTag(CompoundTag param0) {
        if (param0.contains("Inventory", 9)) {
            this.getInventory().fromTag(param0.getList("Inventory", 10));
        }

    }

    default void writeInventoryToTag(CompoundTag param0) {
        param0.put("Inventory", this.getInventory().createTag());
    }
}

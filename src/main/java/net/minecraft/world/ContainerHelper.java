package net.minecraft.world;

import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

public class ContainerHelper {
    public static ItemStack removeItem(List<ItemStack> param0, int param1, int param2) {
        return param1 >= 0 && param1 < param0.size() && !param0.get(param1).isEmpty() && param2 > 0 ? param0.get(param1).split(param2) : ItemStack.EMPTY;
    }

    public static ItemStack takeItem(List<ItemStack> param0, int param1) {
        return param1 >= 0 && param1 < param0.size() ? param0.set(param1, ItemStack.EMPTY) : ItemStack.EMPTY;
    }

    public static CompoundTag saveAllItems(CompoundTag param0, NonNullList<ItemStack> param1) {
        return saveAllItems(param0, param1, true);
    }

    public static CompoundTag saveAllItems(CompoundTag param0, NonNullList<ItemStack> param1, boolean param2) {
        ListTag var0 = new ListTag();

        for(int var1 = 0; var1 < param1.size(); ++var1) {
            ItemStack var2 = param1.get(var1);
            if (!var2.isEmpty()) {
                CompoundTag var3 = new CompoundTag();
                var3.putByte("Slot", (byte)var1);
                var2.save(var3);
                var0.add(var3);
            }
        }

        if (!var0.isEmpty() || param2) {
            param0.put("Items", var0);
        }

        return param0;
    }

    public static void loadAllItems(CompoundTag param0, NonNullList<ItemStack> param1) {
        ListTag var0 = param0.getList("Items", 10);

        for(int var1 = 0; var1 < var0.size(); ++var1) {
            CompoundTag var2 = var0.getCompound(var1);
            int var3 = var2.getByte("Slot") & 255;
            if (var3 >= 0 && var3 < param1.size()) {
                param1.set(var3, ItemStack.of(var2));
            }
        }

    }
}

package net.minecraft.world;

import java.util.Set;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface Container extends Clearable {
    int getContainerSize();

    boolean isEmpty();

    ItemStack getItem(int var1);

    ItemStack removeItem(int var1, int var2);

    ItemStack removeItemNoUpdate(int var1);

    void setItem(int var1, ItemStack var2);

    default int getMaxStackSize() {
        return 64;
    }

    void setChanged();

    boolean stillValid(Player var1);

    default void startOpen(Player param0) {
    }

    default void stopOpen(Player param0) {
    }

    default boolean canPlaceItem(int param0, ItemStack param1) {
        return true;
    }

    default int countItem(Item param0) {
        int var0 = 0;

        for(int var1 = 0; var1 < this.getContainerSize(); ++var1) {
            ItemStack var2 = this.getItem(var1);
            if (var2.getItem().equals(param0)) {
                var0 += var2.getCount();
            }
        }

        return var0;
    }

    default boolean hasAnyOf(Set<Item> param0) {
        for(int var0 = 0; var0 < this.getContainerSize(); ++var0) {
            ItemStack var1 = this.getItem(var0);
            if (param0.contains(var1.getItem()) && var1.getCount() > 0) {
                return true;
            }
        }

        return false;
    }
}

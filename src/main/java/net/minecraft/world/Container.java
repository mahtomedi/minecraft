package net.minecraft.world;

import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface Container extends Clearable {
    int LARGE_MAX_STACK_SIZE = 64;
    int DEFAULT_DISTANCE_LIMIT = 8;

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

    default boolean canTakeItem(Container param0, int param1, ItemStack param2) {
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
        return this.hasAnyMatching(param1 -> !param1.isEmpty() && param0.contains(param1.getItem()));
    }

    default boolean hasAnyMatching(Predicate<ItemStack> param0) {
        for(int var0 = 0; var0 < this.getContainerSize(); ++var0) {
            ItemStack var1 = this.getItem(var0);
            if (param0.test(var1)) {
                return true;
            }
        }

        return false;
    }

    static boolean stillValidBlockEntity(BlockEntity param0, Player param1) {
        return stillValidBlockEntity(param0, param1, 8);
    }

    static boolean stillValidBlockEntity(BlockEntity param0, Player param1, int param2) {
        Level var0 = param0.getLevel();
        BlockPos var1 = param0.getBlockPos();
        if (var0 == null) {
            return false;
        } else if (var0.getBlockEntity(var1) != param0) {
            return false;
        } else {
            return param1.distanceToSqr((double)var1.getX() + 0.5, (double)var1.getY() + 0.5, (double)var1.getZ() + 0.5) <= (double)(param2 * param2);
        }
    }
}

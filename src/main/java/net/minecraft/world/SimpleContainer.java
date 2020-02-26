package net.minecraft.world;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SimpleContainer implements Container, StackedContentsCompatible {
    private final int size;
    private final NonNullList<ItemStack> items;
    private List<ContainerListener> listeners;

    public SimpleContainer(int param0) {
        this.size = param0;
        this.items = NonNullList.withSize(param0, ItemStack.EMPTY);
    }

    public SimpleContainer(ItemStack... param0) {
        this.size = param0.length;
        this.items = NonNullList.of(ItemStack.EMPTY, param0);
    }

    public void addListener(ContainerListener param0) {
        if (this.listeners == null) {
            this.listeners = Lists.newArrayList();
        }

        this.listeners.add(param0);
    }

    public void removeListener(ContainerListener param0) {
        this.listeners.remove(param0);
    }

    @Override
    public ItemStack getItem(int param0) {
        return param0 >= 0 && param0 < this.items.size() ? this.items.get(param0) : ItemStack.EMPTY;
    }

    public List<ItemStack> removeAllItems() {
        List<ItemStack> var0 = this.items.stream().filter(param0 -> !param0.isEmpty()).collect(Collectors.toList());
        this.clearContent();
        return var0;
    }

    @Override
    public ItemStack removeItem(int param0, int param1) {
        ItemStack var0 = ContainerHelper.removeItem(this.items, param0, param1);
        if (!var0.isEmpty()) {
            this.setChanged();
        }

        return var0;
    }

    public ItemStack removeItemType(Item param0, int param1) {
        ItemStack var0 = new ItemStack(param0, 0);

        for(int var1 = this.size - 1; var1 >= 0; --var1) {
            ItemStack var2 = this.getItem(var1);
            if (var2.getItem().equals(param0)) {
                int var3 = param1 - var0.getCount();
                ItemStack var4 = var2.split(var3);
                var0.grow(var4.getCount());
                if (var0.getCount() == param1) {
                    break;
                }
            }
        }

        if (!var0.isEmpty()) {
            this.setChanged();
        }

        return var0;
    }

    public ItemStack addItem(ItemStack param0) {
        ItemStack var0 = param0.copy();
        this.moveItemToOccupiedSlotsWithSameType(var0);
        if (var0.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            this.moveItemToEmptySlots(var0);
            return var0.isEmpty() ? ItemStack.EMPTY : var0;
        }
    }

    @Override
    public ItemStack removeItemNoUpdate(int param0) {
        ItemStack var0 = this.items.get(param0);
        if (var0.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            this.items.set(param0, ItemStack.EMPTY);
            return var0;
        }
    }

    @Override
    public void setItem(int param0, ItemStack param1) {
        this.items.set(param0, param1);
        if (!param1.isEmpty() && param1.getCount() > this.getMaxStackSize()) {
            param1.setCount(this.getMaxStackSize());
        }

        this.setChanged();
    }

    @Override
    public int getContainerSize() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        for(ItemStack var0 : this.items) {
            if (!var0.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void setChanged() {
        if (this.listeners != null) {
            for(ContainerListener var0 : this.listeners) {
                var0.containerChanged(this);
            }
        }

    }

    @Override
    public boolean stillValid(Player param0) {
        return true;
    }

    @Override
    public void clearContent() {
        this.items.clear();
        this.setChanged();
    }

    @Override
    public void fillStackedContents(StackedContents param0) {
        for(ItemStack var0 : this.items) {
            param0.accountStack(var0);
        }

    }

    @Override
    public String toString() {
        return this.items.stream().filter(param0 -> !param0.isEmpty()).collect(Collectors.toList()).toString();
    }

    private void moveItemToEmptySlots(ItemStack param0) {
        for(int var0 = 0; var0 < this.size; ++var0) {
            ItemStack var1 = this.getItem(var0);
            if (var1.isEmpty()) {
                this.setItem(var0, param0.copy());
                param0.setCount(0);
                return;
            }
        }

    }

    private void moveItemToOccupiedSlotsWithSameType(ItemStack param0) {
        for(int var0 = 0; var0 < this.size; ++var0) {
            ItemStack var1 = this.getItem(var0);
            if (this.isSameItem(var1, param0)) {
                this.moveItemsBetweenStacks(param0, var1);
                if (param0.isEmpty()) {
                    return;
                }
            }
        }

    }

    private boolean isSameItem(ItemStack param0, ItemStack param1) {
        return param0.getItem() == param1.getItem() && ItemStack.tagMatches(param0, param1);
    }

    private void moveItemsBetweenStacks(ItemStack param0, ItemStack param1) {
        int var0 = Math.min(this.getMaxStackSize(), param1.getMaxStackSize());
        int var1 = Math.min(param0.getCount(), var0 - param1.getCount());
        if (var1 > 0) {
            param1.grow(var1);
            param0.shrink(var1);
            this.setChanged();
        }

    }
}

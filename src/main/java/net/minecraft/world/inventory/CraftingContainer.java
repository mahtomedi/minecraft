package net.minecraft.world.inventory;

import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;

public class CraftingContainer implements Container, StackedContentsCompatible {
    private final NonNullList<ItemStack> items;
    private final int width;
    private final int height;
    private final AbstractContainerMenu menu;

    public CraftingContainer(AbstractContainerMenu param0, int param1, int param2) {
        this.items = NonNullList.withSize(param1 * param2, ItemStack.EMPTY);
        this.menu = param0;
        this.width = param1;
        this.height = param2;
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
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
    public ItemStack getItem(int param0) {
        return param0 >= this.getContainerSize() ? ItemStack.EMPTY : this.items.get(param0);
    }

    @Override
    public ItemStack removeItemNoUpdate(int param0) {
        return ContainerHelper.takeItem(this.items, param0);
    }

    @Override
    public ItemStack removeItem(int param0, int param1) {
        ItemStack var0 = ContainerHelper.removeItem(this.items, param0, param1);
        if (!var0.isEmpty()) {
            this.menu.slotsChanged(this);
        }

        return var0;
    }

    @Override
    public void setItem(int param0, ItemStack param1) {
        this.items.set(param0, param1);
        this.menu.slotsChanged(this);
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player param0) {
        return true;
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }

    public List<ItemStack> getItems() {
        return List.copyOf(this.items);
    }

    @Override
    public void fillStackedContents(StackedContents param0) {
        for(ItemStack var0 : this.items) {
            param0.accountSimpleStack(var0);
        }

    }
}

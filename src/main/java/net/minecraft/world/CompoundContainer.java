package net.minecraft.world;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CompoundContainer implements Container {
    private final Container container1;
    private final Container container2;

    public CompoundContainer(Container param0, Container param1) {
        this.container1 = param0;
        this.container2 = param1;
    }

    @Override
    public int getContainerSize() {
        return this.container1.getContainerSize() + this.container2.getContainerSize();
    }

    @Override
    public boolean isEmpty() {
        return this.container1.isEmpty() && this.container2.isEmpty();
    }

    public boolean contains(Container param0) {
        return this.container1 == param0 || this.container2 == param0;
    }

    @Override
    public ItemStack getItem(int param0) {
        return param0 >= this.container1.getContainerSize()
            ? this.container2.getItem(param0 - this.container1.getContainerSize())
            : this.container1.getItem(param0);
    }

    @Override
    public ItemStack removeItem(int param0, int param1) {
        return param0 >= this.container1.getContainerSize()
            ? this.container2.removeItem(param0 - this.container1.getContainerSize(), param1)
            : this.container1.removeItem(param0, param1);
    }

    @Override
    public ItemStack removeItemNoUpdate(int param0) {
        return param0 >= this.container1.getContainerSize()
            ? this.container2.removeItemNoUpdate(param0 - this.container1.getContainerSize())
            : this.container1.removeItemNoUpdate(param0);
    }

    @Override
    public void setItem(int param0, ItemStack param1) {
        if (param0 >= this.container1.getContainerSize()) {
            this.container2.setItem(param0 - this.container1.getContainerSize(), param1);
        } else {
            this.container1.setItem(param0, param1);
        }

    }

    @Override
    public int getMaxStackSize() {
        return this.container1.getMaxStackSize();
    }

    @Override
    public void setChanged() {
        this.container1.setChanged();
        this.container2.setChanged();
    }

    @Override
    public boolean stillValid(Player param0) {
        return this.container1.stillValid(param0) && this.container2.stillValid(param0);
    }

    @Override
    public void startOpen(Player param0) {
        this.container1.startOpen(param0);
        this.container2.startOpen(param0);
    }

    @Override
    public void stopOpen(Player param0) {
        this.container1.stopOpen(param0);
        this.container2.stopOpen(param0);
    }

    @Override
    public boolean canPlaceItem(int param0, ItemStack param1) {
        return param0 >= this.container1.getContainerSize()
            ? this.container2.canPlaceItem(param0 - this.container1.getContainerSize(), param1)
            : this.container1.canPlaceItem(param0, param1);
    }

    @Override
    public void clearContent() {
        this.container1.clearContent();
        this.container2.clearContent();
    }
}

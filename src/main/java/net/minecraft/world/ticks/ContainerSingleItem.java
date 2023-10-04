package net.minecraft.world.ticks;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface ContainerSingleItem extends Container {
    ItemStack getTheItem();

    ItemStack splitTheItem(int var1);

    void setTheItem(ItemStack var1);

    BlockEntity getContainerBlockEntity();

    default ItemStack removeTheItem() {
        return this.splitTheItem(this.getMaxStackSize());
    }

    @Override
    default int getContainerSize() {
        return 1;
    }

    @Override
    default boolean isEmpty() {
        return this.getTheItem().isEmpty();
    }

    @Override
    default void clearContent() {
        this.removeTheItem();
    }

    @Override
    default ItemStack removeItemNoUpdate(int param0) {
        return this.removeItem(param0, this.getMaxStackSize());
    }

    @Override
    default ItemStack getItem(int param0) {
        return param0 == 0 ? this.getTheItem() : ItemStack.EMPTY;
    }

    @Override
    default ItemStack removeItem(int param0, int param1) {
        return param0 != 0 ? ItemStack.EMPTY : this.splitTheItem(param1);
    }

    @Override
    default void setItem(int param0, ItemStack param1) {
        if (param0 == 0) {
            this.setTheItem(param1);
        }

    }

    @Override
    default boolean stillValid(Player param0) {
        return Container.stillValidBlockEntity(this.getContainerBlockEntity(), param0);
    }
}

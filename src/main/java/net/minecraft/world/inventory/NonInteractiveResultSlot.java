package net.minecraft.world.inventory;

import java.util.Optional;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class NonInteractiveResultSlot extends Slot {
    public NonInteractiveResultSlot(Container param0, int param1, int param2, int param3) {
        super(param0, param1, param2, param3);
    }

    @Override
    public void onQuickCraft(ItemStack param0, ItemStack param1) {
    }

    @Override
    public boolean mayPickup(Player param0) {
        return false;
    }

    @Override
    public Optional<ItemStack> tryRemove(int param0, int param1, Player param2) {
        return Optional.empty();
    }

    @Override
    public ItemStack safeTake(int param0, int param1, Player param2) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack safeInsert(ItemStack param0) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack safeInsert(ItemStack param0, int param1) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean allowModification(Player param0) {
        return false;
    }

    @Override
    public boolean mayPlace(ItemStack param0) {
        return false;
    }

    @Override
    public ItemStack remove(int param0) {
        return ItemStack.EMPTY;
    }

    @Override
    public void onTake(Player param0, ItemStack param1) {
    }

    @Override
    public boolean isHighlightable() {
        return false;
    }
}

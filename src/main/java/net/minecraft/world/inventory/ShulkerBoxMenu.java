package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ShulkerBoxMenu extends AbstractContainerMenu {
    private final Container container;

    public ShulkerBoxMenu(int param0, Inventory param1) {
        this(param0, param1, new SimpleContainer(27));
    }

    public ShulkerBoxMenu(int param0, Inventory param1, Container param2) {
        super(MenuType.SHULKER_BOX, param0);
        checkContainerSize(param2, 27);
        this.container = param2;
        param2.startOpen(param1.player);
        int var0 = 3;
        int var1 = 9;

        for(int var2 = 0; var2 < 3; ++var2) {
            for(int var3 = 0; var3 < 9; ++var3) {
                this.addSlot(new ShulkerBoxSlot(param2, var3 + var2 * 9, 8 + var3 * 18, 18 + var2 * 18));
            }
        }

        for(int var4 = 0; var4 < 3; ++var4) {
            for(int var5 = 0; var5 < 9; ++var5) {
                this.addSlot(new Slot(param1, var5 + var4 * 9 + 9, 8 + var5 * 18, 84 + var4 * 18));
            }
        }

        for(int var6 = 0; var6 < 9; ++var6) {
            this.addSlot(new Slot(param1, var6, 8 + var6 * 18, 142));
        }

    }

    @Override
    public boolean stillValid(Player param0) {
        return this.container.stillValid(param0);
    }

    @Override
    public ItemStack quickMoveStack(Player param0, int param1) {
        ItemStack var0 = ItemStack.EMPTY;
        Slot var1 = this.slots.get(param1);
        if (var1 != null && var1.hasItem()) {
            ItemStack var2 = var1.getItem();
            var0 = var2.copy();
            if (param1 < this.container.getContainerSize()) {
                if (!this.moveItemStackTo(var2, this.container.getContainerSize(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(var2, 0, this.container.getContainerSize(), false)) {
                return ItemStack.EMPTY;
            }

            if (var2.isEmpty()) {
                var1.set(ItemStack.EMPTY);
            } else {
                var1.setChanged();
            }
        }

        return var0;
    }

    @Override
    public void removed(Player param0) {
        super.removed(param0);
        this.container.stopOpen(param0);
    }
}

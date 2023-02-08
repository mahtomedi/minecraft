package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class HopperMenu extends AbstractContainerMenu {
    public static final int CONTAINER_SIZE = 5;
    private final Container hopper;

    public HopperMenu(int param0, Inventory param1) {
        this(param0, param1, new SimpleContainer(5));
    }

    public HopperMenu(int param0, Inventory param1, Container param2) {
        super(MenuType.HOPPER, param0);
        this.hopper = param2;
        checkContainerSize(param2, 5);
        param2.startOpen(param1.player);
        int var0 = 51;

        for(int var1 = 0; var1 < 5; ++var1) {
            this.addSlot(new Slot(param2, var1, 44 + var1 * 18, 20));
        }

        for(int var2 = 0; var2 < 3; ++var2) {
            for(int var3 = 0; var3 < 9; ++var3) {
                this.addSlot(new Slot(param1, var3 + var2 * 9 + 9, 8 + var3 * 18, var2 * 18 + 51));
            }
        }

        for(int var4 = 0; var4 < 9; ++var4) {
            this.addSlot(new Slot(param1, var4, 8 + var4 * 18, 109));
        }

    }

    @Override
    public boolean stillValid(Player param0) {
        return this.hopper.stillValid(param0);
    }

    @Override
    public ItemStack quickMoveStack(Player param0, int param1) {
        ItemStack var0 = ItemStack.EMPTY;
        Slot var1 = this.slots.get(param1);
        if (var1 != null && var1.hasItem()) {
            ItemStack var2 = var1.getItem();
            var0 = var2.copy();
            if (param1 < this.hopper.getContainerSize()) {
                if (!this.moveItemStackTo(var2, this.hopper.getContainerSize(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(var2, 0, this.hopper.getContainerSize(), false)) {
                return ItemStack.EMPTY;
            }

            if (var2.isEmpty()) {
                var1.setByPlayer(ItemStack.EMPTY);
            } else {
                var1.setChanged();
            }
        }

        return var0;
    }

    @Override
    public void removed(Player param0) {
        super.removed(param0);
        this.hopper.stopOpen(param0);
    }
}

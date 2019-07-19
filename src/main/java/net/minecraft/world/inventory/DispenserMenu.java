package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class DispenserMenu extends AbstractContainerMenu {
    private final Container dispenser;

    public DispenserMenu(int param0, Inventory param1) {
        this(param0, param1, new SimpleContainer(9));
    }

    public DispenserMenu(int param0, Inventory param1, Container param2) {
        super(MenuType.GENERIC_3x3, param0);
        checkContainerSize(param2, 9);
        this.dispenser = param2;
        param2.startOpen(param1.player);

        for(int var0 = 0; var0 < 3; ++var0) {
            for(int var1 = 0; var1 < 3; ++var1) {
                this.addSlot(new Slot(param2, var1 + var0 * 3, 62 + var1 * 18, 17 + var0 * 18));
            }
        }

        for(int var2 = 0; var2 < 3; ++var2) {
            for(int var3 = 0; var3 < 9; ++var3) {
                this.addSlot(new Slot(param1, var3 + var2 * 9 + 9, 8 + var3 * 18, 84 + var2 * 18));
            }
        }

        for(int var4 = 0; var4 < 9; ++var4) {
            this.addSlot(new Slot(param1, var4, 8 + var4 * 18, 142));
        }

    }

    @Override
    public boolean stillValid(Player param0) {
        return this.dispenser.stillValid(param0);
    }

    @Override
    public ItemStack quickMoveStack(Player param0, int param1) {
        ItemStack var0 = ItemStack.EMPTY;
        Slot var1 = this.slots.get(param1);
        if (var1 != null && var1.hasItem()) {
            ItemStack var2 = var1.getItem();
            var0 = var2.copy();
            if (param1 < 9) {
                if (!this.moveItemStackTo(var2, 9, 45, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(var2, 0, 9, false)) {
                return ItemStack.EMPTY;
            }

            if (var2.isEmpty()) {
                var1.set(ItemStack.EMPTY);
            } else {
                var1.setChanged();
            }

            if (var2.getCount() == var0.getCount()) {
                return ItemStack.EMPTY;
            }

            var1.onTake(param0, var2);
        }

        return var0;
    }

    @Override
    public void removed(Player param0) {
        super.removed(param0);
        this.dispenser.stopOpen(param0);
    }
}

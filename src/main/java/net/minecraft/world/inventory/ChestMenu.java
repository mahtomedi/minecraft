package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ChestMenu extends AbstractContainerMenu {
    private static final int SLOTS_PER_ROW = 9;
    private final Container container;
    private final int containerRows;

    private ChestMenu(MenuType<?> param0, int param1, Inventory param2, int param3) {
        this(param0, param1, param2, new SimpleContainer(9 * param3), param3);
    }

    public static ChestMenu oneRow(int param0, Inventory param1) {
        return new ChestMenu(MenuType.GENERIC_9x1, param0, param1, 1);
    }

    public static ChestMenu twoRows(int param0, Inventory param1) {
        return new ChestMenu(MenuType.GENERIC_9x2, param0, param1, 2);
    }

    public static ChestMenu threeRows(int param0, Inventory param1) {
        return new ChestMenu(MenuType.GENERIC_9x3, param0, param1, 3);
    }

    public static ChestMenu fourRows(int param0, Inventory param1) {
        return new ChestMenu(MenuType.GENERIC_9x4, param0, param1, 4);
    }

    public static ChestMenu fiveRows(int param0, Inventory param1) {
        return new ChestMenu(MenuType.GENERIC_9x5, param0, param1, 5);
    }

    public static ChestMenu sixRows(int param0, Inventory param1) {
        return new ChestMenu(MenuType.GENERIC_9x6, param0, param1, 6);
    }

    public static ChestMenu threeRows(int param0, Inventory param1, Container param2) {
        return new ChestMenu(MenuType.GENERIC_9x3, param0, param1, param2, 3);
    }

    public static ChestMenu sixRows(int param0, Inventory param1, Container param2) {
        return new ChestMenu(MenuType.GENERIC_9x6, param0, param1, param2, 6);
    }

    public ChestMenu(MenuType<?> param0, int param1, Inventory param2, Container param3, int param4) {
        super(param0, param1);
        checkContainerSize(param3, param4 * 9);
        this.container = param3;
        this.containerRows = param4;
        param3.startOpen(param2.player);
        int var0 = (this.containerRows - 4) * 18;

        for(int var1 = 0; var1 < this.containerRows; ++var1) {
            for(int var2 = 0; var2 < 9; ++var2) {
                this.addSlot(new Slot(param3, var2 + var1 * 9, 8 + var2 * 18, 18 + var1 * 18));
            }
        }

        for(int var3 = 0; var3 < 3; ++var3) {
            for(int var4 = 0; var4 < 9; ++var4) {
                this.addSlot(new Slot(param2, var4 + var3 * 9 + 9, 8 + var4 * 18, 103 + var3 * 18 + var0));
            }
        }

        for(int var5 = 0; var5 < 9; ++var5) {
            this.addSlot(new Slot(param2, var5, 8 + var5 * 18, 161 + var0));
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
            if (param1 < this.containerRows * 9) {
                if (!this.moveItemStackTo(var2, this.containerRows * 9, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(var2, 0, this.containerRows * 9, false)) {
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
        this.container.stopOpen(param0);
    }

    public Container getContainer() {
        return this.container;
    }

    public int getRowCount() {
        return this.containerRows;
    }
}

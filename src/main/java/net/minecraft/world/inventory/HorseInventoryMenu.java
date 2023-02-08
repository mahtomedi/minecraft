package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class HorseInventoryMenu extends AbstractContainerMenu {
    private final Container horseContainer;
    private final AbstractHorse horse;

    public HorseInventoryMenu(int param0, Inventory param1, Container param2, final AbstractHorse param3) {
        super(null, param0);
        this.horseContainer = param2;
        this.horse = param3;
        int var0 = 3;
        param2.startOpen(param1.player);
        int var1 = -18;
        this.addSlot(new Slot(param2, 0, 8, 18) {
            @Override
            public boolean mayPlace(ItemStack param0) {
                return param0.is(Items.SADDLE) && !this.hasItem() && param3.isSaddleable();
            }

            @Override
            public boolean isActive() {
                return param3.isSaddleable();
            }
        });
        this.addSlot(new Slot(param2, 1, 8, 36) {
            @Override
            public boolean mayPlace(ItemStack param0) {
                return param3.isArmor(param0);
            }

            @Override
            public boolean isActive() {
                return param3.canWearArmor();
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        if (this.hasChest(param3)) {
            for(int var2 = 0; var2 < 3; ++var2) {
                for(int var3 = 0; var3 < ((AbstractChestedHorse)param3).getInventoryColumns(); ++var3) {
                    this.addSlot(new Slot(param2, 2 + var3 + var2 * ((AbstractChestedHorse)param3).getInventoryColumns(), 80 + var3 * 18, 18 + var2 * 18));
                }
            }
        }

        for(int var4 = 0; var4 < 3; ++var4) {
            for(int var5 = 0; var5 < 9; ++var5) {
                this.addSlot(new Slot(param1, var5 + var4 * 9 + 9, 8 + var5 * 18, 102 + var4 * 18 + -18));
            }
        }

        for(int var6 = 0; var6 < 9; ++var6) {
            this.addSlot(new Slot(param1, var6, 8 + var6 * 18, 142));
        }

    }

    @Override
    public boolean stillValid(Player param0) {
        return !this.horse.hasInventoryChanged(this.horseContainer)
            && this.horseContainer.stillValid(param0)
            && this.horse.isAlive()
            && this.horse.distanceTo(param0) < 8.0F;
    }

    private boolean hasChest(AbstractHorse param0) {
        return param0 instanceof AbstractChestedHorse && ((AbstractChestedHorse)param0).hasChest();
    }

    @Override
    public ItemStack quickMoveStack(Player param0, int param1) {
        ItemStack var0 = ItemStack.EMPTY;
        Slot var1 = this.slots.get(param1);
        if (var1 != null && var1.hasItem()) {
            ItemStack var2 = var1.getItem();
            var0 = var2.copy();
            int var3 = this.horseContainer.getContainerSize();
            if (param1 < var3) {
                if (!this.moveItemStackTo(var2, var3, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.getSlot(1).mayPlace(var2) && !this.getSlot(1).hasItem()) {
                if (!this.moveItemStackTo(var2, 1, 2, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.getSlot(0).mayPlace(var2)) {
                if (!this.moveItemStackTo(var2, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (var3 <= 2 || !this.moveItemStackTo(var2, 2, var3, false)) {
                int var5 = var3 + 27;
                int var7 = var5 + 9;
                if (param1 >= var5 && param1 < var7) {
                    if (!this.moveItemStackTo(var2, var3, var5, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (param1 >= var3 && param1 < var5) {
                    if (!this.moveItemStackTo(var2, var5, var7, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(var2, var5, var5, false)) {
                    return ItemStack.EMPTY;
                }

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
        this.horseContainer.stopOpen(param0);
    }
}

package net.minecraft.world.inventory;

import javax.annotation.Nullable;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public abstract class ItemCombinerMenu extends AbstractContainerMenu {
    protected final ResultContainer resultSlots = new ResultContainer();
    protected final Container inputSlots = new SimpleContainer(2) {
        @Override
        public void setChanged() {
            super.setChanged();
            ItemCombinerMenu.this.slotsChanged(this);
        }
    };
    protected final ContainerLevelAccess access;
    protected final Player player;

    protected abstract boolean mayPickup(Player var1, boolean var2);

    protected abstract ItemStack onTake(Player var1, ItemStack var2);

    protected abstract boolean isValidBlock(BlockState var1);

    public ItemCombinerMenu(@Nullable MenuType<?> param0, int param1, Inventory param2, ContainerLevelAccess param3) {
        super(param0, param1);
        this.access = param3;
        this.player = param2.player;
        this.addSlot(new Slot(this.inputSlots, 0, 27, 47));
        this.addSlot(new Slot(this.inputSlots, 1, 76, 47));
        this.addSlot(new Slot(this.resultSlots, 2, 134, 47) {
            @Override
            public boolean mayPlace(ItemStack param0) {
                return false;
            }

            @Override
            public boolean mayPickup(Player param0) {
                return ItemCombinerMenu.this.mayPickup(param0, this.hasItem());
            }

            @Override
            public ItemStack onTake(Player param0, ItemStack param1) {
                return ItemCombinerMenu.this.onTake(param0, param1);
            }
        });

        for(int var0 = 0; var0 < 3; ++var0) {
            for(int var1 = 0; var1 < 9; ++var1) {
                this.addSlot(new Slot(param2, var1 + var0 * 9 + 9, 8 + var1 * 18, 84 + var0 * 18));
            }
        }

        for(int var2 = 0; var2 < 9; ++var2) {
            this.addSlot(new Slot(param2, var2, 8 + var2 * 18, 142));
        }

    }

    public abstract void createResult();

    @Override
    public void slotsChanged(Container param0) {
        super.slotsChanged(param0);
        if (param0 == this.inputSlots) {
            this.createResult();
        }

    }

    @Override
    public void removed(Player param0) {
        super.removed(param0);
        this.access.execute((param1, param2) -> this.clearContainer(param0, this.inputSlots));
    }

    @Override
    public boolean stillValid(Player param0) {
        return this.access
            .evaluate(
                (param1, param2) -> !this.isValidBlock(param1.getBlockState(param2))
                        ? false
                        : param0.distanceToSqr((double)param2.getX() + 0.5, (double)param2.getY() + 0.5, (double)param2.getZ() + 0.5) <= 64.0,
                true
            );
    }

    protected boolean shouldQuickMoveToAdditionalSlot(ItemStack param0) {
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player param0, int param1) {
        ItemStack var0 = ItemStack.EMPTY;
        Slot var1 = this.slots.get(param1);
        if (var1 != null && var1.hasItem()) {
            ItemStack var2 = var1.getItem();
            var0 = var2.copy();
            if (param1 == 2) {
                if (!this.moveItemStackTo(var2, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }

                var1.onQuickCraft(var2, var0);
            } else if (param1 != 0 && param1 != 1) {
                if (param1 >= 3 && param1 < 39) {
                    int var3 = this.shouldQuickMoveToAdditionalSlot(var0) ? 1 : 0;
                    if (!this.moveItemStackTo(var2, var3, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!this.moveItemStackTo(var2, 3, 39, false)) {
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
}

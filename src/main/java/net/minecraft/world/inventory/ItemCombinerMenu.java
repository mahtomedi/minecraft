package net.minecraft.world.inventory;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public abstract class ItemCombinerMenu extends AbstractContainerMenu {
    private static final int INVENTORY_SLOTS_PER_ROW = 9;
    private static final int INVENTORY_SLOTS_PER_COLUMN = 3;
    protected final ContainerLevelAccess access;
    protected final Player player;
    protected final Container inputSlots;
    private final List<Integer> inputSlotIndexes;
    protected final ResultContainer resultSlots = new ResultContainer();
    private final int resultSlotIndex;

    protected abstract boolean mayPickup(Player var1, boolean var2);

    protected abstract void onTake(Player var1, ItemStack var2);

    protected abstract boolean isValidBlock(BlockState var1);

    public ItemCombinerMenu(@Nullable MenuType<?> param0, int param1, Inventory param2, ContainerLevelAccess param3) {
        super(param0, param1);
        this.access = param3;
        this.player = param2.player;
        ItemCombinerMenuSlotDefinition var0 = this.createInputSlotDefinitions();
        this.inputSlots = this.createContainer(var0.getNumOfInputSlots());
        this.inputSlotIndexes = var0.getInputSlotIndexes();
        this.resultSlotIndex = var0.getResultSlotIndex();
        this.createInputSlots(var0);
        this.createResultSlot(var0);
        this.createInventorySlots(param2);
    }

    private void createInputSlots(ItemCombinerMenuSlotDefinition param0) {
        for(final ItemCombinerMenuSlotDefinition.SlotDefinition var0 : param0.getSlots()) {
            this.addSlot(new Slot(this.inputSlots, var0.slotIndex(), var0.x(), var0.y()) {
                @Override
                public boolean mayPlace(ItemStack param0) {
                    return var0.mayPlace().test(param0);
                }
            });
        }

    }

    private void createResultSlot(ItemCombinerMenuSlotDefinition param0) {
        this.addSlot(new Slot(this.resultSlots, param0.getResultSlot().slotIndex(), param0.getResultSlot().x(), param0.getResultSlot().y()) {
            @Override
            public boolean mayPlace(ItemStack param0) {
                return false;
            }

            @Override
            public boolean mayPickup(Player param0) {
                return ItemCombinerMenu.this.mayPickup(param0, this.hasItem());
            }

            @Override
            public void onTake(Player param0, ItemStack param1) {
                ItemCombinerMenu.this.onTake(param0, param1);
            }
        });
    }

    private void createInventorySlots(Inventory param0) {
        for(int var0 = 0; var0 < 3; ++var0) {
            for(int var1 = 0; var1 < 9; ++var1) {
                this.addSlot(new Slot(param0, var1 + var0 * 9 + 9, 8 + var1 * 18, 84 + var0 * 18));
            }
        }

        for(int var2 = 0; var2 < 9; ++var2) {
            this.addSlot(new Slot(param0, var2, 8 + var2 * 18, 142));
        }

    }

    public abstract void createResult();

    protected abstract ItemCombinerMenuSlotDefinition createInputSlotDefinitions();

    private SimpleContainer createContainer(int param0) {
        return new SimpleContainer(param0) {
            @Override
            public void setChanged() {
                super.setChanged();
                ItemCombinerMenu.this.slotsChanged(this);
            }
        };
    }

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

    @Override
    public ItemStack quickMoveStack(Player param0, int param1) {
        ItemStack var0 = ItemStack.EMPTY;
        Slot var1 = this.slots.get(param1);
        if (var1 != null && var1.hasItem()) {
            ItemStack var2 = var1.getItem();
            var0 = var2.copy();
            int var3 = this.getInventorySlotStart();
            int var4 = this.getUseRowEnd();
            if (param1 == this.getResultSlot()) {
                if (!this.moveItemStackTo(var2, var3, var4, true)) {
                    return ItemStack.EMPTY;
                }

                var1.onQuickCraft(var2, var0);
            } else if (this.inputSlotIndexes.contains(param1)) {
                if (!this.moveItemStackTo(var2, var3, var4, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.canMoveIntoInputSlots(var2) && param1 >= this.getInventorySlotStart() && param1 < this.getUseRowEnd()) {
                int var5 = this.getSlotToQuickMoveTo(var0);
                if (!this.moveItemStackTo(var2, var5, this.getResultSlot(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if (param1 >= this.getInventorySlotStart() && param1 < this.getInventorySlotEnd()) {
                if (!this.moveItemStackTo(var2, this.getUseRowStart(), this.getUseRowEnd(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if (param1 >= this.getUseRowStart()
                && param1 < this.getUseRowEnd()
                && !this.moveItemStackTo(var2, this.getInventorySlotStart(), this.getInventorySlotEnd(), false)) {
                return ItemStack.EMPTY;
            }

            if (var2.isEmpty()) {
                var1.setByPlayer(ItemStack.EMPTY);
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

    protected boolean canMoveIntoInputSlots(ItemStack param0) {
        return true;
    }

    public int getSlotToQuickMoveTo(ItemStack param0) {
        return this.inputSlots.isEmpty() ? 0 : this.inputSlotIndexes.get(0);
    }

    public int getResultSlot() {
        return this.resultSlotIndex;
    }

    private int getInventorySlotStart() {
        return this.getResultSlot() + 1;
    }

    private int getInventorySlotEnd() {
        return this.getInventorySlotStart() + 27;
    }

    private int getUseRowStart() {
        return this.getInventorySlotEnd();
    }

    private int getUseRowEnd() {
        return this.getUseRowStart() + 9;
    }
}

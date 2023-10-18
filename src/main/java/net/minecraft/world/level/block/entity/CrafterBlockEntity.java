package net.minecraft.world.level.block.entity;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.CrafterMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.block.state.BlockState;

public class CrafterBlockEntity extends RandomizableContainerBlockEntity implements CraftingContainer {
    public static final int CONTAINER_WIDTH = 3;
    public static final int CONTAINER_HEIGHT = 3;
    public static final int CONTAINER_SIZE = 9;
    public static final int SLOT_DISABLED = 1;
    public static final int SLOT_ENABLED = 0;
    public static final int DATA_TRIGGERED = 9;
    public static final int NUM_DATA = 10;
    private NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);
    private int craftingTicksRemaining = 0;
    protected final ContainerData containerData = new ContainerData() {
        private final int[] slotStates = new int[9];
        private int triggered = 0;

        @Override
        public int get(int param0) {
            return param0 == 9 ? this.triggered : this.slotStates[param0];
        }

        @Override
        public void set(int param0, int param1) {
            if (param0 == 9) {
                this.triggered = param1;
            } else {
                this.slotStates[param0] = param1;
            }

        }

        @Override
        public int getCount() {
            return 10;
        }
    };

    public CrafterBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.CRAFTER, param0, param1);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.crafter");
    }

    @Override
    protected AbstractContainerMenu createMenu(int param0, Inventory param1) {
        return new CrafterMenu(param0, param1, this, this.containerData);
    }

    public void setSlotState(int param0, boolean param1) {
        if (this.slotCanBeDisabled(param0)) {
            this.containerData.set(param0, param1 ? 0 : 1);
            this.setChanged();
        }
    }

    public boolean isSlotDisabled(int param0) {
        if (param0 >= 0 && param0 < 9) {
            return this.containerData.get(param0) == 1;
        } else {
            return false;
        }
    }

    @Override
    public boolean canPlaceItem(int param0, ItemStack param1) {
        if (this.containerData.get(param0) == 1) {
            return false;
        } else {
            ItemStack var0 = this.items.get(param0);
            int var1 = var0.getCount();
            if (var1 >= var0.getMaxStackSize()) {
                return false;
            } else if (var0.isEmpty()) {
                return true;
            } else {
                return !this.smallerStackExist(var1, var0, param0);
            }
        }
    }

    private boolean smallerStackExist(int param0, ItemStack param1, int param2) {
        for(int var0 = param2 + 1; var0 < 9; ++var0) {
            if (!this.isSlotDisabled(var0)) {
                ItemStack var1 = this.getItem(var0);
                if (var1.isEmpty() || var1.getCount() < param0 && ItemStack.isSameItemSameTags(var1, param1)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void load(CompoundTag param0) {
        super.load(param0);
        this.craftingTicksRemaining = param0.getInt("crafting_ticks_remaining");
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(param0)) {
            ContainerHelper.loadAllItems(param0, this.items);
        }

        int[] var0 = param0.getIntArray("disabled_slots");

        for(int var1 = 0; var1 < 9; ++var1) {
            this.containerData.set(var1, 0);
        }

        for(int var2 : var0) {
            if (this.slotCanBeDisabled(var2)) {
                this.containerData.set(var2, 1);
            }
        }

        this.containerData.set(9, param0.getInt("triggered"));
    }

    @Override
    protected void saveAdditional(CompoundTag param0) {
        super.saveAdditional(param0);
        param0.putInt("crafting_ticks_remaining", this.craftingTicksRemaining);
        if (!this.trySaveLootTable(param0)) {
            ContainerHelper.saveAllItems(param0, this.items);
        }

        this.addDisabledSlots(param0);
        this.addTriggered(param0);
    }

    @Override
    public int getContainerSize() {
        return 9;
    }

    @Override
    public boolean isEmpty() {
        for(ItemStack var0 : this.items) {
            if (!var0.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack getItem(int param0) {
        return this.items.get(param0);
    }

    @Override
    public void setItem(int param0, ItemStack param1) {
        if (this.isSlotDisabled(param0)) {
            this.setSlotState(param0, true);
        }

        super.setItem(param0, param1);
    }

    @Override
    public boolean stillValid(Player param0) {
        if (this.level != null && this.level.getBlockEntity(this.worldPosition) == this) {
            return !(
                param0.distanceToSqr((double)this.worldPosition.getX() + 0.5, (double)this.worldPosition.getY() + 0.5, (double)this.worldPosition.getZ() + 0.5)
                    > 64.0
            );
        } else {
            return false;
        }
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> param0) {
        this.items = param0;
    }

    @Override
    public int getWidth() {
        return 3;
    }

    @Override
    public int getHeight() {
        return 3;
    }

    @Override
    public void fillStackedContents(StackedContents param0) {
        for(ItemStack var0 : this.items) {
            param0.accountSimpleStack(var0);
        }

    }

    private void addDisabledSlots(CompoundTag param0) {
        IntList var0 = new IntArrayList();

        for(int var1 = 0; var1 < 9; ++var1) {
            if (this.isSlotDisabled(var1)) {
                var0.add(var1);
            }
        }

        param0.putIntArray("disabled_slots", (List<Integer>)var0);
    }

    private void addTriggered(CompoundTag param0) {
        param0.putInt("triggered", this.containerData.get(9));
    }

    public void setTriggered(boolean param0) {
        this.containerData.set(9, param0 ? 1 : 0);
    }

    @VisibleForTesting
    public boolean isTriggered() {
        return this.containerData.get(9) == 1;
    }

    public static void serverTick(Level param0, BlockPos param1, BlockState param2, CrafterBlockEntity param3) {
        int var0 = param3.craftingTicksRemaining - 1;
        if (var0 >= 0) {
            param3.craftingTicksRemaining = var0;
            if (var0 == 0) {
                param0.setBlock(param1, param2.setValue(CrafterBlock.CRAFTING, Boolean.valueOf(false)), 3);
            }

        }
    }

    public void setCraftingTicksRemaining(int param0) {
        this.craftingTicksRemaining = param0;
    }

    public int getRedstoneSignal() {
        int var0 = 0;

        for(int var1 = 0; var1 < this.getContainerSize(); ++var1) {
            ItemStack var2 = this.getItem(var1);
            if (!var2.isEmpty() || this.isSlotDisabled(var1)) {
                ++var0;
            }
        }

        return var0;
    }

    private boolean slotCanBeDisabled(int param0) {
        return param0 > -1 && param0 < 9 && this.items.get(param0).isEmpty();
    }
}

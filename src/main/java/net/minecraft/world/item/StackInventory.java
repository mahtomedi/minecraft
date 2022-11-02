package net.minecraft.world.item;

import java.util.Arrays;
import java.util.List;

public final class StackInventory {
    private int top = -1;
    private final boolean[] freeSlots;
    private final int[] slotMap;
    private final ItemStack[] items;
    private final int capacity;

    public StackInventory(int param0) {
        this.capacity = param0;
        this.freeSlots = new boolean[param0];
        Arrays.fill(this.freeSlots, true);
        this.slotMap = new int[param0];
        this.items = new ItemStack[param0];
    }

    private int firstFreeSlot() {
        for(int var0 = 0; var0 < this.freeSlots.length; ++var0) {
            if (this.freeSlots[var0]) {
                return var0;
            }
        }

        return -1;
    }

    public boolean pushWithSlot(ItemStack param0, int param1) {
        if (this.top != this.capacity - 1 && !param0.isEmpty()) {
            ++this.top;
            this.slotMap[this.top] = param1;
            this.freeSlots[param1] = false;
            this.items[this.top] = param0;
            return true;
        } else {
            return false;
        }
    }

    public boolean push(ItemStack param0) {
        return this.pushWithSlot(param0, this.firstFreeSlot());
    }

    public ItemStack pop() {
        if (this.top == -1) {
            return ItemStack.EMPTY;
        } else {
            ItemStack var0 = this.items[this.top];
            this.items[this.top] = ItemStack.EMPTY;
            this.freeSlots[this.slotMap[this.top]] = true;
            --this.top;
            return var0;
        }
    }

    public boolean canSet(int param0) {
        return param0 >= 0 && param0 <= this.capacity - 1;
    }

    public boolean set(ItemStack param0, int param1) {
        if (param1 >= 0 && param1 <= this.capacity - 1) {
            if (param0.isEmpty()) {
                this.remove(param1);
                return true;
            } else {
                for(int var0 = 0; var0 < this.size(); ++var0) {
                    if (this.slotMap[var0] == param1) {
                        this.items[var0] = param0;
                        return true;
                    }
                }

                return this.pushWithSlot(param0, param1);
            }
        } else {
            return false;
        }
    }

    public ItemStack get(int param0) {
        for(int var0 = 0; var0 < this.size(); ++var0) {
            if (this.slotMap[var0] == param0) {
                return this.items[var0];
            }
        }

        return ItemStack.EMPTY;
    }

    public ItemStack remove(int param0) {
        for(int var0 = 0; var0 < this.size(); ++var0) {
            if (this.slotMap[var0] == param0) {
                ItemStack var1 = this.items[var0];
                if (var0 != this.top) {
                    System.arraycopy(this.slotMap, var0 + 1, this.slotMap, var0, this.top - var0);
                    System.arraycopy(this.items, var0 + 1, this.items, var0, this.top - var0);
                }

                this.freeSlots[param0] = true;
                --this.top;
                return var1;
            }
        }

        return ItemStack.EMPTY;
    }

    public int size() {
        return this.top + 1;
    }

    public boolean isFull() {
        return this.size() == this.capacity;
    }

    public boolean isEmpty() {
        return this.top == -1;
    }

    public List<ItemStack> view() {
        ItemStack[] var0 = new ItemStack[this.size()];
        System.arraycopy(this.items, 0, var0, 0, this.size());
        return List.of(var0);
    }

    public List<ItemStack> clear() {
        if (this.top == -1) {
            return List.of();
        } else {
            List<ItemStack> var0 = this.view();

            for(int var1 = 0; var1 < this.size(); ++var1) {
                this.items[var1] = ItemStack.EMPTY;
                this.freeSlots[var1] = true;
            }

            this.top = -1;
            return var0;
        }
    }

    public StackInventory.FlattenResult flatten() {
        StackInventory.FlattenResult var0 = StackInventory.FlattenResult.NO_CHANGE;

        for(int var1 = 0; var1 < this.size(); ++var1) {
            for(int var2 = this.items[var1].getCount() - 1; var2 > 0; --var2) {
                var0 = StackInventory.FlattenResult.FULLY_FLATTENED;
                if (!this.push(this.items[var1].split(1))) {
                    this.items[var1].grow(1);
                    return StackInventory.FlattenResult.PARTIALLY_FLATTENED;
                }
            }
        }

        return var0;
    }

    public static enum FlattenResult {
        PARTIALLY_FLATTENED,
        FULLY_FLATTENED,
        NO_CHANGE;
    }
}

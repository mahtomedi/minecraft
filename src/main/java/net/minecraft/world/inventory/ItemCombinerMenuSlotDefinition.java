package net.minecraft.world.inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.world.item.ItemStack;

public class ItemCombinerMenuSlotDefinition {
    private final List<ItemCombinerMenuSlotDefinition.SlotDefinition> slots;
    private final ItemCombinerMenuSlotDefinition.SlotDefinition resultSlot;

    ItemCombinerMenuSlotDefinition(List<ItemCombinerMenuSlotDefinition.SlotDefinition> param0, ItemCombinerMenuSlotDefinition.SlotDefinition param1) {
        if (!param0.isEmpty() && !param1.equals(ItemCombinerMenuSlotDefinition.SlotDefinition.EMPTY)) {
            this.slots = param0;
            this.resultSlot = param1;
        } else {
            throw new IllegalArgumentException("Need to define both inputSlots and resultSlot");
        }
    }

    public static ItemCombinerMenuSlotDefinition.Builder create() {
        return new ItemCombinerMenuSlotDefinition.Builder();
    }

    public boolean hasSlot(int param0) {
        return this.slots.size() >= param0;
    }

    public ItemCombinerMenuSlotDefinition.SlotDefinition getSlot(int param0) {
        return this.slots.get(param0);
    }

    public ItemCombinerMenuSlotDefinition.SlotDefinition getResultSlot() {
        return this.resultSlot;
    }

    public List<ItemCombinerMenuSlotDefinition.SlotDefinition> getSlots() {
        return this.slots;
    }

    public int getNumOfInputSlots() {
        return this.slots.size();
    }

    public int getResultSlotIndex() {
        return this.getNumOfInputSlots();
    }

    public List<Integer> getInputSlotIndexes() {
        return this.slots.stream().map(ItemCombinerMenuSlotDefinition.SlotDefinition::slotIndex).collect(Collectors.toList());
    }

    public static class Builder {
        private final List<ItemCombinerMenuSlotDefinition.SlotDefinition> slots = new ArrayList<>();
        private ItemCombinerMenuSlotDefinition.SlotDefinition resultSlot = ItemCombinerMenuSlotDefinition.SlotDefinition.EMPTY;

        public ItemCombinerMenuSlotDefinition.Builder withSlot(int param0, int param1, int param2, Predicate<ItemStack> param3) {
            this.slots.add(new ItemCombinerMenuSlotDefinition.SlotDefinition(param0, param1, param2, param3));
            return this;
        }

        public ItemCombinerMenuSlotDefinition.Builder withResultSlot(int param0, int param1, int param2) {
            this.resultSlot = new ItemCombinerMenuSlotDefinition.SlotDefinition(param0, param1, param2, param0x -> false);
            return this;
        }

        public ItemCombinerMenuSlotDefinition build() {
            return new ItemCombinerMenuSlotDefinition(this.slots, this.resultSlot);
        }
    }

    public static record SlotDefinition(int slotIndex, int x, int y, Predicate<ItemStack> mayPlace) {
        static final ItemCombinerMenuSlotDefinition.SlotDefinition EMPTY = new ItemCombinerMenuSlotDefinition.SlotDefinition(0, 0, 0, param0 -> true);
    }
}

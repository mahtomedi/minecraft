package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class ShulkerBoxSlot extends Slot {
    public ShulkerBoxSlot(Container param0, int param1, int param2, int param3) {
        super(param0, param1, param2, param3);
    }

    @Override
    public boolean mayPlace(ItemStack param0) {
        return param0.getItem().canFitInsideContainerItems();
    }
}

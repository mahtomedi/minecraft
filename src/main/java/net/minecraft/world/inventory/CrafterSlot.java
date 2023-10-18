package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class CrafterSlot extends Slot {
    private final CrafterMenu menu;

    public CrafterSlot(Container param0, int param1, int param2, int param3, CrafterMenu param4) {
        super(param0, param1, param2, param3);
        this.menu = param4;
    }

    @Override
    public boolean mayPlace(ItemStack param0) {
        return !this.menu.isSlotDisabled(this.index) && super.mayPlace(param0);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        this.menu.slotsChanged(this.container);
    }
}

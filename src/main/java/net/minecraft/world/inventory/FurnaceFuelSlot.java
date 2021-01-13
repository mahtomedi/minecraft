package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class FurnaceFuelSlot extends Slot {
    private final AbstractFurnaceMenu menu;

    public FurnaceFuelSlot(AbstractFurnaceMenu param0, Container param1, int param2, int param3, int param4) {
        super(param1, param2, param3, param4);
        this.menu = param0;
    }

    @Override
    public boolean mayPlace(ItemStack param0) {
        return this.menu.isFuel(param0) || isBucket(param0);
    }

    @Override
    public int getMaxStackSize(ItemStack param0) {
        return isBucket(param0) ? 1 : super.getMaxStackSize(param0);
    }

    public static boolean isBucket(ItemStack param0) {
        return param0.getItem() == Items.BUCKET;
    }
}

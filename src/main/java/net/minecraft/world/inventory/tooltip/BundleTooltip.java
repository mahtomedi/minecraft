package net.minecraft.world.inventory.tooltip;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BundleTooltip implements TooltipComponent {
    private final NonNullList<ItemStack> items;
    private final boolean showEmptySlot;

    public BundleTooltip(NonNullList<ItemStack> param0, boolean param1) {
        this.items = param0;
        this.showEmptySlot = param1;
    }

    public NonNullList<ItemStack> getItems() {
        return this.items;
    }

    public boolean showEmptySlot() {
        return this.showEmptySlot;
    }
}

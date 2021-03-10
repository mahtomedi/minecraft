package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CreativeInventoryListener implements ContainerListener {
    private final Minecraft minecraft;

    public CreativeInventoryListener(Minecraft param0) {
        this.minecraft = param0;
    }

    @Override
    public void slotChanged(AbstractContainerMenu param0, int param1, ItemStack param2) {
        this.minecraft.gameMode.handleCreativeModeItemAdd(param2, param1);
    }

    @Override
    public void dataChanged(AbstractContainerMenu param0, int param1, int param2) {
    }
}

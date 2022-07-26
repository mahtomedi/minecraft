package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BlastFurnaceMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;

public class BlastFurnaceBlockEntity extends AbstractFurnaceBlockEntity {
    public BlastFurnaceBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.BLAST_FURNACE, param0, param1, RecipeType.BLASTING);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.blast_furnace");
    }

    @Override
    protected int getBurnDuration(ItemStack param0) {
        return super.getBurnDuration(param0) / 2;
    }

    @Override
    protected AbstractContainerMenu createMenu(int param0, Inventory param1) {
        return new BlastFurnaceMenu(param0, param1, this, this.dataAccess);
    }
}

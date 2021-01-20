package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SmokerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;

public class SmokerBlockEntity extends AbstractFurnaceBlockEntity {
    public SmokerBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.SMOKER, param0, param1, RecipeType.SMOKING);
    }

    @Override
    protected Component getDefaultName() {
        return new TranslatableComponent("container.smoker");
    }

    @Override
    protected int getBurnDuration(ItemStack param0) {
        return super.getBurnDuration(param0) / 2;
    }

    @Override
    protected AbstractContainerMenu createMenu(int param0, Inventory param1) {
        return new SmokerMenu(param0, param1, this, this.dataAccess);
    }
}

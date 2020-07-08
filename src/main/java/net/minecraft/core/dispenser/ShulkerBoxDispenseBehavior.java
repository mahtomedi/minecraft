package net.minecraft.core.dispenser;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.block.DispenserBlock;

public class ShulkerBoxDispenseBehavior extends OptionalDispenseItemBehavior {
    @Override
    protected ItemStack execute(BlockSource param0, ItemStack param1) {
        this.setSuccess(false);
        Item var0 = param1.getItem();
        if (var0 instanceof BlockItem) {
            Direction var1 = param0.getBlockState().getValue(DispenserBlock.FACING);
            BlockPos var2 = param0.getPos().relative(var1);
            Direction var3 = param0.getLevel().isEmptyBlock(var2.below()) ? var1 : Direction.UP;
            this.setSuccess(((BlockItem)var0).place(new DirectionalPlaceContext(param0.getLevel(), var2, var1, param1, var3)).consumesAction());
        }

        return param1;
    }
}

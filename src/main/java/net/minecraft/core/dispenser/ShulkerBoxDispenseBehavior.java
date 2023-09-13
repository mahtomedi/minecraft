package net.minecraft.core.dispenser;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.block.DispenserBlock;
import org.slf4j.Logger;

public class ShulkerBoxDispenseBehavior extends OptionalDispenseItemBehavior {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    protected ItemStack execute(BlockSource param0, ItemStack param1) {
        this.setSuccess(false);
        Item var0 = param1.getItem();
        if (var0 instanceof BlockItem) {
            Direction var1 = param0.state().getValue(DispenserBlock.FACING);
            BlockPos var2 = param0.pos().relative(var1);
            Direction var3 = param0.level().isEmptyBlock(var2.below()) ? var1 : Direction.UP;

            try {
                this.setSuccess(((BlockItem)var0).place(new DirectionalPlaceContext(param0.level(), var2, var1, param1, var3)).consumesAction());
            } catch (Exception var8) {
                LOGGER.error("Error trying to place shulker box at {}", var2, var8);
            }
        }

        return param1;
    }
}

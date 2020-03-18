package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ShearsItem extends Item {
    public ShearsItem(Item.Properties param0) {
        super(param0);
    }

    @Override
    public boolean mineBlock(ItemStack param0, Level param1, BlockState param2, BlockPos param3, LivingEntity param4) {
        if (!param1.isClientSide && !param2.getBlock().is(BlockTags.FIRE)) {
            param0.hurtAndBreak(1, param4, param0x -> param0x.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }

        Block var0 = param2.getBlock();
        return !param2.is(BlockTags.LEAVES)
                && var0 != Blocks.COBWEB
                && var0 != Blocks.GRASS
                && var0 != Blocks.FERN
                && var0 != Blocks.DEAD_BUSH
                && var0 != Blocks.VINE
                && var0 != Blocks.TRIPWIRE
                && !var0.is(BlockTags.WOOL)
            ? super.mineBlock(param0, param1, param2, param3, param4)
            : true;
    }

    @Override
    public boolean canDestroySpecial(BlockState param0) {
        Block var0 = param0.getBlock();
        return var0 == Blocks.COBWEB || var0 == Blocks.REDSTONE_WIRE || var0 == Blocks.TRIPWIRE;
    }

    @Override
    public float getDestroySpeed(ItemStack param0, BlockState param1) {
        Block var0 = param1.getBlock();
        if (var0 == Blocks.COBWEB || param1.is(BlockTags.LEAVES)) {
            return 15.0F;
        } else {
            return var0.is(BlockTags.WOOL) ? 5.0F : super.getDestroySpeed(param0, param1);
        }
    }
}

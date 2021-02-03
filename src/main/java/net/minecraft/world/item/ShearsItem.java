package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ShearsItem extends Item {
    public ShearsItem(Item.Properties param0) {
        super(param0);
    }

    @Override
    public boolean mineBlock(ItemStack param0, Level param1, BlockState param2, BlockPos param3, LivingEntity param4) {
        if (!param1.isClientSide && !param2.is(BlockTags.FIRE)) {
            param0.hurtAndBreak(1, param4, param0x -> param0x.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }

        return !param2.is(BlockTags.LEAVES)
                && !param2.is(Blocks.COBWEB)
                && !param2.is(Blocks.GRASS)
                && !param2.is(Blocks.FERN)
                && !param2.is(Blocks.DEAD_BUSH)
                && !param2.is(Blocks.HANGING_ROOTS)
                && !param2.is(Blocks.VINE)
                && !param2.is(Blocks.TRIPWIRE)
                && !param2.is(BlockTags.WOOL)
            ? super.mineBlock(param0, param1, param2, param3, param4)
            : true;
    }

    @Override
    public boolean isCorrectToolForDrops(BlockState param0) {
        return param0.is(Blocks.COBWEB) || param0.is(Blocks.REDSTONE_WIRE) || param0.is(Blocks.TRIPWIRE);
    }

    @Override
    public float getDestroySpeed(ItemStack param0, BlockState param1) {
        if (param1.is(Blocks.COBWEB) || param1.is(BlockTags.LEAVES)) {
            return 15.0F;
        } else if (param1.is(BlockTags.WOOL)) {
            return 5.0F;
        } else {
            return !param1.is(Blocks.VINE) && !param1.is(Blocks.GLOW_LICHEN) ? super.getDestroySpeed(param0, param1) : 2.0F;
        }
    }
}

package net.minecraft.world.item;

import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class DoubleHighBlockItem extends BlockItem {
    public DoubleHighBlockItem(Block param0, Item.Properties param1) {
        super(param0, param1);
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext param0, BlockState param1) {
        param0.getLevel().setBlock(param0.getClickedPos().above(), Blocks.AIR.defaultBlockState(), 27);
        return super.placeBlock(param0, param1);
    }
}

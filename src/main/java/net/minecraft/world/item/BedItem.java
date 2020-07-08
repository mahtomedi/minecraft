package net.minecraft.world.item;

import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BedItem extends BlockItem {
    public BedItem(Block param0, Item.Properties param1) {
        super(param0, param1);
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext param0, BlockState param1) {
        return param0.getLevel().setBlock(param0.getClickedPos(), param1, 26);
    }
}

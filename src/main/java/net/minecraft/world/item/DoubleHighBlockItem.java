package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class DoubleHighBlockItem extends BlockItem {
    public DoubleHighBlockItem(Block param0, Item.Properties param1) {
        super(param0, param1);
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext param0, BlockState param1) {
        Level var0 = param0.getLevel();
        BlockPos var1 = param0.getClickedPos().above();
        BlockState var2 = var0.isWaterAt(var1) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
        var0.setBlock(var1, var2, 27);
        return super.placeBlock(param0, param1);
    }
}

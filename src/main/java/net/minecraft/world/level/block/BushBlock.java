package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.BlockLayer;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public class BushBlock extends Block {
    protected BushBlock(Block.Properties param0) {
        super(param0);
    }

    protected boolean mayPlaceOn(BlockState param0, BlockGetter param1, BlockPos param2) {
        Block var0 = param0.getBlock();
        return var0 == Blocks.GRASS_BLOCK || var0 == Blocks.DIRT || var0 == Blocks.COARSE_DIRT || var0 == Blocks.PODZOL || var0 == Blocks.FARMLAND;
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        return !param0.canSurvive(param3, param4) ? Blocks.AIR.defaultBlockState() : super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        BlockPos var0 = param2.below();
        return this.mayPlaceOn(param1.getBlockState(var0), param1, var0);
    }

    @Override
    public BlockLayer getRenderLayer() {
        return BlockLayer.CUTOUT;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState param0, BlockGetter param1, BlockPos param2) {
        return true;
    }
}

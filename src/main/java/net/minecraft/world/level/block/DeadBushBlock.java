package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DeadBushBlock extends BushBlock {
    protected static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 13.0, 14.0);

    protected DeadBushBlock(Block.Properties param0) {
        super(param0);
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState param0, BlockGetter param1, BlockPos param2) {
        Block var0 = param0.getBlock();
        return var0 == Blocks.SAND
            || var0 == Blocks.RED_SAND
            || var0 == Blocks.TERRACOTTA
            || var0 == Blocks.WHITE_TERRACOTTA
            || var0 == Blocks.ORANGE_TERRACOTTA
            || var0 == Blocks.MAGENTA_TERRACOTTA
            || var0 == Blocks.LIGHT_BLUE_TERRACOTTA
            || var0 == Blocks.YELLOW_TERRACOTTA
            || var0 == Blocks.LIME_TERRACOTTA
            || var0 == Blocks.PINK_TERRACOTTA
            || var0 == Blocks.GRAY_TERRACOTTA
            || var0 == Blocks.LIGHT_GRAY_TERRACOTTA
            || var0 == Blocks.CYAN_TERRACOTTA
            || var0 == Blocks.PURPLE_TERRACOTTA
            || var0 == Blocks.BLUE_TERRACOTTA
            || var0 == Blocks.BROWN_TERRACOTTA
            || var0 == Blocks.GREEN_TERRACOTTA
            || var0 == Blocks.RED_TERRACOTTA
            || var0 == Blocks.BLACK_TERRACOTTA
            || var0 == Blocks.DIRT
            || var0 == Blocks.COARSE_DIRT
            || var0 == Blocks.PODZOL;
    }
}

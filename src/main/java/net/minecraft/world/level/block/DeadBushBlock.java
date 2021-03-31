package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DeadBushBlock extends BushBlock {
    protected static final float AABB_OFFSET = 6.0F;
    protected static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 13.0, 14.0);

    protected DeadBushBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState param0, BlockGetter param1, BlockPos param2) {
        return param0.is(Blocks.SAND)
            || param0.is(Blocks.RED_SAND)
            || param0.is(Blocks.TERRACOTTA)
            || param0.is(Blocks.WHITE_TERRACOTTA)
            || param0.is(Blocks.ORANGE_TERRACOTTA)
            || param0.is(Blocks.MAGENTA_TERRACOTTA)
            || param0.is(Blocks.LIGHT_BLUE_TERRACOTTA)
            || param0.is(Blocks.YELLOW_TERRACOTTA)
            || param0.is(Blocks.LIME_TERRACOTTA)
            || param0.is(Blocks.PINK_TERRACOTTA)
            || param0.is(Blocks.GRAY_TERRACOTTA)
            || param0.is(Blocks.LIGHT_GRAY_TERRACOTTA)
            || param0.is(Blocks.CYAN_TERRACOTTA)
            || param0.is(Blocks.PURPLE_TERRACOTTA)
            || param0.is(Blocks.BLUE_TERRACOTTA)
            || param0.is(Blocks.BROWN_TERRACOTTA)
            || param0.is(Blocks.GREEN_TERRACOTTA)
            || param0.is(Blocks.RED_TERRACOTTA)
            || param0.is(Blocks.BLACK_TERRACOTTA)
            || param0.is(BlockTags.DIRT);
    }
}

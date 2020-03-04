package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WeepingVines extends GrowingPlantHeadBlock {
    protected static final VoxelShape SHAPE = Block.box(4.0, 9.0, 4.0, 12.0, 16.0, 12.0);

    public WeepingVines(Block.Properties param0) {
        super(param0, Direction.DOWN, false, 0.1);
    }

    @Override
    protected boolean canGrowInto(BlockState param0) {
        return param0.isAir();
    }

    @Override
    protected Block getBodyBlock() {
        return Blocks.WEEPING_VINES_PLANT;
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPE;
    }
}

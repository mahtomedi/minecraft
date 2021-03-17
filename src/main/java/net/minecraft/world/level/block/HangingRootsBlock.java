package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HangingRootsBlock extends Block {
    protected static final VoxelShape SHAPE = Block.box(4.0, 9.0, 4.0, 12.0, 16.0, 12.0);

    protected HangingRootsBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        BlockPos var0 = param2.above();
        BlockState var1 = param1.getBlockState(var0);
        return var1.isFaceSturdy(param1, var0, Direction.DOWN);
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPE;
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        return param1 == Direction.UP && !this.canSurvive(param0, param3, param4)
            ? Blocks.AIR.defaultBlockState()
            : super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public BlockBehaviour.OffsetType getOffsetType() {
        return BlockBehaviour.OffsetType.XZ;
    }
}

package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SoulSandBlock extends Block {
    protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 14.0, 16.0);
    private static final int BUBBLE_COLUMN_CHECK_DELAY = 20;

    public SoulSandBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPE;
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState param0, BlockGetter param1, BlockPos param2) {
        return Shapes.block();
    }

    @Override
    public VoxelShape getVisualShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return Shapes.block();
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        BubbleColumnBlock.updateColumn(param1, param2.above(), param0);
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param1 == Direction.UP && param2.is(Blocks.WATER)) {
            param3.scheduleTick(param4, this, 20);
        }

        return super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public void onPlace(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        param1.scheduleTick(param2, this, 20);
    }

    @Override
    public boolean isPathfindable(BlockState param0, BlockGetter param1, BlockPos param2, PathComputationType param3) {
        return false;
    }
}

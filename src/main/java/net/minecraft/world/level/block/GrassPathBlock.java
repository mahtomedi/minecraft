package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GrassPathBlock extends Block {
    protected static final VoxelShape SHAPE = FarmBlock.SHAPE;

    protected GrassPathBlock(Block.Properties param0) {
        super(param0);
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState param0) {
        return true;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        return !this.defaultBlockState().canSurvive(param0.getLevel(), param0.getClickedPos())
            ? Block.pushEntitiesUp(this.defaultBlockState(), Blocks.DIRT.defaultBlockState(), param0.getLevel(), param0.getClickedPos())
            : super.getStateForPlacement(param0);
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param1 == Direction.UP && !param0.canSurvive(param3, param4)) {
            param3.getBlockTicks().scheduleTick(param4, this, 1);
        }

        return super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        FarmBlock.turnToDirt(param0, param1, param2);
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        BlockState var0 = param1.getBlockState(param2.above());
        return !var0.getMaterial().isSolid() || var0.getBlock() instanceof FenceGateBlock;
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPE;
    }

    @Override
    public boolean isPathfindable(BlockState param0, BlockGetter param1, BlockPos param2, PathComputationType param3) {
        return false;
    }

    @Override
    public boolean isViewBlocking(BlockState param0, BlockGetter param1, BlockPos param2) {
        return true;
    }
}

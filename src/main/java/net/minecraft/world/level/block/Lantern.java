package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class Lantern extends Block {
    public static final BooleanProperty HANGING = BlockStateProperties.HANGING;
    protected static final VoxelShape AABB = Shapes.or(Block.box(5.0, 0.0, 5.0, 11.0, 7.0, 11.0), Block.box(6.0, 7.0, 6.0, 10.0, 9.0, 10.0));
    protected static final VoxelShape HANGING_AABB = Shapes.or(Block.box(5.0, 1.0, 5.0, 11.0, 8.0, 11.0), Block.box(6.0, 8.0, 6.0, 10.0, 10.0, 10.0));

    public Lantern(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(HANGING, Boolean.valueOf(false)));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        for(Direction var0 : param0.getNearestLookingDirections()) {
            if (var0.getAxis() == Direction.Axis.Y) {
                BlockState var1 = this.defaultBlockState().setValue(HANGING, Boolean.valueOf(var0 == Direction.UP));
                if (var1.canSurvive(param0.getLevel(), param0.getClickedPos())) {
                    return var1;
                }
            }
        }

        return null;
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return param0.getValue(HANGING) ? HANGING_AABB : AABB;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(HANGING);
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        Direction var0 = getConnectedDirection(param0).getOpposite();
        return Block.canSupportCenter(param1, param2.relative(var0), var0.getOpposite());
    }

    protected static Direction getConnectedDirection(BlockState param0) {
        return param0.getValue(HANGING) ? Direction.DOWN : Direction.UP;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState param0) {
        return PushReaction.DESTROY;
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        return getConnectedDirection(param0).getOpposite() == param1 && !param0.canSurvive(param3, param4)
            ? Blocks.AIR.defaultBlockState()
            : super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public boolean isPathfindable(BlockState param0, BlockGetter param1, BlockPos param2, PathComputationType param3) {
        return false;
    }
}

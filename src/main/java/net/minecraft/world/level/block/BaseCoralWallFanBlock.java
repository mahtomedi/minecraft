package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BaseCoralWallFanBlock extends BaseCoralFanBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final Map<Direction, VoxelShape> SHAPES = Maps.newEnumMap(
        ImmutableMap.of(
            Direction.NORTH,
            Block.box(0.0, 4.0, 5.0, 16.0, 12.0, 16.0),
            Direction.SOUTH,
            Block.box(0.0, 4.0, 0.0, 16.0, 12.0, 11.0),
            Direction.WEST,
            Block.box(5.0, 4.0, 0.0, 16.0, 12.0, 16.0),
            Direction.EAST,
            Block.box(0.0, 4.0, 0.0, 11.0, 12.0, 16.0)
        )
    );

    protected BaseCoralWallFanBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, Boolean.valueOf(true)));
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPES.get(param0.getValue(FACING));
    }

    @Override
    public BlockState rotate(BlockState param0, Rotation param1) {
        return param0.setValue(FACING, param1.rotate(param0.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState param0, Mirror param1) {
        return param0.rotate(param1.getRotation(param0.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(FACING, WATERLOGGED);
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param0.getValue(WATERLOGGED)) {
            param3.getLiquidTicks().scheduleTick(param4, Fluids.WATER, Fluids.WATER.getTickDelay(param3));
        }

        return param1.getOpposite() == param0.getValue(FACING) && !param0.canSurvive(param3, param4) ? Blocks.AIR.defaultBlockState() : param0;
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        Direction var0 = param0.getValue(FACING);
        BlockPos var1 = param2.relative(var0.getOpposite());
        BlockState var2 = param1.getBlockState(var1);
        return var2.isFaceSturdy(param1, var1, var0);
    }

    @Override
    public boolean isUnstable() {
        return true;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        BlockState var0 = super.getStateForPlacement(param0);
        LevelReader var1 = param0.getLevel();
        BlockPos var2 = param0.getClickedPos();
        Direction[] var3 = param0.getNearestLookingDirections();

        for(Direction var4 : var3) {
            if (var4.getAxis().isHorizontal()) {
                var0 = var0.setValue(FACING, var4.getOpposite());
                if (var0.canSurvive(var1, var2)) {
                    return var0;
                }
            }
        }

        return null;
    }
}

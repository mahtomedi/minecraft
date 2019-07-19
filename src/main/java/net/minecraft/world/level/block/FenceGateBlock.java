package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FenceGateBlock extends HorizontalDirectionalBlock {
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty IN_WALL = BlockStateProperties.IN_WALL;
    protected static final VoxelShape Z_SHAPE = Block.box(0.0, 0.0, 6.0, 16.0, 16.0, 10.0);
    protected static final VoxelShape X_SHAPE = Block.box(6.0, 0.0, 0.0, 10.0, 16.0, 16.0);
    protected static final VoxelShape Z_SHAPE_LOW = Block.box(0.0, 0.0, 6.0, 16.0, 13.0, 10.0);
    protected static final VoxelShape X_SHAPE_LOW = Block.box(6.0, 0.0, 0.0, 10.0, 13.0, 16.0);
    protected static final VoxelShape Z_COLLISION_SHAPE = Block.box(0.0, 0.0, 6.0, 16.0, 24.0, 10.0);
    protected static final VoxelShape X_COLLISION_SHAPE = Block.box(6.0, 0.0, 0.0, 10.0, 24.0, 16.0);
    protected static final VoxelShape Z_OCCLUSION_SHAPE = Shapes.or(Block.box(0.0, 5.0, 7.0, 2.0, 16.0, 9.0), Block.box(14.0, 5.0, 7.0, 16.0, 16.0, 9.0));
    protected static final VoxelShape X_OCCLUSION_SHAPE = Shapes.or(Block.box(7.0, 5.0, 0.0, 9.0, 16.0, 2.0), Block.box(7.0, 5.0, 14.0, 9.0, 16.0, 16.0));
    protected static final VoxelShape Z_OCCLUSION_SHAPE_LOW = Shapes.or(Block.box(0.0, 2.0, 7.0, 2.0, 13.0, 9.0), Block.box(14.0, 2.0, 7.0, 16.0, 13.0, 9.0));
    protected static final VoxelShape X_OCCLUSION_SHAPE_LOW = Shapes.or(Block.box(7.0, 2.0, 0.0, 9.0, 13.0, 2.0), Block.box(7.0, 2.0, 14.0, 9.0, 13.0, 16.0));

    public FenceGateBlock(Block.Properties param0) {
        super(param0);
        this.registerDefaultState(
            this.stateDefinition
                .any()
                .setValue(OPEN, Boolean.valueOf(false))
                .setValue(POWERED, Boolean.valueOf(false))
                .setValue(IN_WALL, Boolean.valueOf(false))
        );
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        if (param0.getValue(IN_WALL)) {
            return param0.getValue(FACING).getAxis() == Direction.Axis.X ? X_SHAPE_LOW : Z_SHAPE_LOW;
        } else {
            return param0.getValue(FACING).getAxis() == Direction.Axis.X ? X_SHAPE : Z_SHAPE;
        }
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        Direction.Axis var0 = param1.getAxis();
        if (param0.getValue(FACING).getClockWise().getAxis() != var0) {
            return super.updateShape(param0, param1, param2, param3, param4, param5);
        } else {
            boolean var1 = this.isWall(param2) || this.isWall(param3.getBlockState(param4.relative(param1.getOpposite())));
            return param0.setValue(IN_WALL, Boolean.valueOf(var1));
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        if (param0.getValue(OPEN)) {
            return Shapes.empty();
        } else {
            return param0.getValue(FACING).getAxis() == Direction.Axis.Z ? Z_COLLISION_SHAPE : X_COLLISION_SHAPE;
        }
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState param0, BlockGetter param1, BlockPos param2) {
        if (param0.getValue(IN_WALL)) {
            return param0.getValue(FACING).getAxis() == Direction.Axis.X ? X_OCCLUSION_SHAPE_LOW : Z_OCCLUSION_SHAPE_LOW;
        } else {
            return param0.getValue(FACING).getAxis() == Direction.Axis.X ? X_OCCLUSION_SHAPE : Z_OCCLUSION_SHAPE;
        }
    }

    @Override
    public boolean isPathfindable(BlockState param0, BlockGetter param1, BlockPos param2, PathComputationType param3) {
        switch(param3) {
            case LAND:
                return param0.getValue(OPEN);
            case WATER:
                return false;
            case AIR:
                return param0.getValue(OPEN);
            default:
                return false;
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        Level var0 = param0.getLevel();
        BlockPos var1 = param0.getClickedPos();
        boolean var2 = var0.hasNeighborSignal(var1);
        Direction var3 = param0.getHorizontalDirection();
        Direction.Axis var4 = var3.getAxis();
        boolean var5 = var4 == Direction.Axis.Z && (this.isWall(var0.getBlockState(var1.west())) || this.isWall(var0.getBlockState(var1.east())))
            || var4 == Direction.Axis.X && (this.isWall(var0.getBlockState(var1.north())) || this.isWall(var0.getBlockState(var1.south())));
        return this.defaultBlockState()
            .setValue(FACING, var3)
            .setValue(OPEN, Boolean.valueOf(var2))
            .setValue(POWERED, Boolean.valueOf(var2))
            .setValue(IN_WALL, Boolean.valueOf(var5));
    }

    private boolean isWall(BlockState param0) {
        return param0.getBlock().is(BlockTags.WALLS);
    }

    @Override
    public boolean use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        if (param0.getValue(OPEN)) {
            param0 = param0.setValue(OPEN, Boolean.valueOf(false));
            param1.setBlock(param2, param0, 10);
        } else {
            Direction var0 = param3.getDirection();
            if (param0.getValue(FACING) == var0.getOpposite()) {
                param0 = param0.setValue(FACING, var0);
            }

            param0 = param0.setValue(OPEN, Boolean.valueOf(true));
            param1.setBlock(param2, param0, 10);
        }

        param1.levelEvent(param3, param0.getValue(OPEN) ? 1008 : 1014, param2, 0);
        return true;
    }

    @Override
    public void neighborChanged(BlockState param0, Level param1, BlockPos param2, Block param3, BlockPos param4, boolean param5) {
        if (!param1.isClientSide) {
            boolean var0 = param1.hasNeighborSignal(param2);
            if (param0.getValue(POWERED) != var0) {
                param1.setBlock(param2, param0.setValue(POWERED, Boolean.valueOf(var0)).setValue(OPEN, Boolean.valueOf(var0)), 2);
                if (param0.getValue(OPEN) != var0) {
                    param1.levelEvent(null, var0 ? 1008 : 1014, param2, 0);
                }
            }

        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(FACING, OPEN, POWERED, IN_WALL);
    }

    public static boolean connectsToDirection(BlockState param0, Direction param1) {
        return param0.getValue(FACING).getAxis() == param1.getClockWise().getAxis();
    }
}

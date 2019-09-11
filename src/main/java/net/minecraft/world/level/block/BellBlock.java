package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BellBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final EnumProperty<BellAttachType> ATTACHMENT = BlockStateProperties.BELL_ATTACHMENT;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private static final VoxelShape NORTH_SOUTH_FLOOR_SHAPE = Block.box(0.0, 0.0, 4.0, 16.0, 16.0, 12.0);
    private static final VoxelShape EAST_WEST_FLOOR_SHAPE = Block.box(4.0, 0.0, 0.0, 12.0, 16.0, 16.0);
    private static final VoxelShape BELL_TOP_SHAPE = Block.box(5.0, 6.0, 5.0, 11.0, 13.0, 11.0);
    private static final VoxelShape BELL_BOTTOM_SHAPE = Block.box(4.0, 4.0, 4.0, 12.0, 6.0, 12.0);
    private static final VoxelShape BELL_SHAPE = Shapes.or(BELL_BOTTOM_SHAPE, BELL_TOP_SHAPE);
    private static final VoxelShape NORTH_SOUTH_BETWEEN = Shapes.or(BELL_SHAPE, Block.box(7.0, 13.0, 0.0, 9.0, 15.0, 16.0));
    private static final VoxelShape EAST_WEST_BETWEEN = Shapes.or(BELL_SHAPE, Block.box(0.0, 13.0, 7.0, 16.0, 15.0, 9.0));
    private static final VoxelShape TO_WEST = Shapes.or(BELL_SHAPE, Block.box(0.0, 13.0, 7.0, 13.0, 15.0, 9.0));
    private static final VoxelShape TO_EAST = Shapes.or(BELL_SHAPE, Block.box(3.0, 13.0, 7.0, 16.0, 15.0, 9.0));
    private static final VoxelShape TO_NORTH = Shapes.or(BELL_SHAPE, Block.box(7.0, 13.0, 0.0, 9.0, 15.0, 13.0));
    private static final VoxelShape TO_SOUTH = Shapes.or(BELL_SHAPE, Block.box(7.0, 13.0, 3.0, 9.0, 15.0, 16.0));
    private static final VoxelShape CEILING_SHAPE = Shapes.or(BELL_SHAPE, Block.box(7.0, 13.0, 7.0, 9.0, 16.0, 9.0));

    public BellBlock(Block.Properties param0) {
        super(param0);
        this.registerDefaultState(
            this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ATTACHMENT, BellAttachType.FLOOR).setValue(POWERED, Boolean.valueOf(false))
        );
    }

    @Override
    public void neighborChanged(BlockState param0, Level param1, BlockPos param2, Block param3, BlockPos param4, boolean param5) {
        boolean var0 = param1.hasNeighborSignal(param2);
        if (var0 != param0.getValue(POWERED)) {
            if (var0) {
                this.attemptToRing(param1, param2, null);
            }

            param1.setBlock(param2, param0.setValue(POWERED, Boolean.valueOf(var0)), 3);
        }

    }

    @Override
    public void onProjectileHit(Level param0, BlockState param1, BlockHitResult param2, Entity param3) {
        if (param3 instanceof AbstractArrow) {
            Entity var0 = ((AbstractArrow)param3).getOwner();
            Player var1 = var0 instanceof Player ? (Player)var0 : null;
            this.onHit(param0, param1, param2, var1, true);
        }

    }

    @Override
    public boolean use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        return this.onHit(param1, param0, param5, param3, true);
    }

    public boolean onHit(Level param0, BlockState param1, BlockHitResult param2, @Nullable Player param3, boolean param4) {
        Direction var0 = param2.getDirection();
        BlockPos var1 = param2.getBlockPos();
        boolean var2 = !param4 || this.isProperHit(param1, var0, param2.getLocation().y - (double)var1.getY());
        if (var2) {
            boolean var3 = this.attemptToRing(param0, var1, var0);
            if (var3 && param3 != null) {
                param3.awardStat(Stats.BELL_RING);
            }
        }

        return true;
    }

    private boolean isProperHit(BlockState param0, Direction param1, double param2) {
        if (param1.getAxis() != Direction.Axis.Y && !(param2 > 0.8124F)) {
            Direction var0 = param0.getValue(FACING);
            BellAttachType var1 = param0.getValue(ATTACHMENT);
            switch(var1) {
                case FLOOR:
                    return var0.getAxis() == param1.getAxis();
                case SINGLE_WALL:
                case DOUBLE_WALL:
                    return var0.getAxis() != param1.getAxis();
                case CEILING:
                    return true;
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

    public boolean attemptToRing(Level param0, BlockPos param1, @Nullable Direction param2) {
        BlockEntity var0 = param0.getBlockEntity(param1);
        if (!param0.isClientSide && var0 instanceof BellBlockEntity) {
            if (param2 == null) {
                param2 = param0.getBlockState(param1).getValue(FACING);
            }

            ((BellBlockEntity)var0).onHit(param2);
            param0.playSound(null, param1, SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, 2.0F, 1.0F);
            return true;
        } else {
            return false;
        }
    }

    private VoxelShape getVoxelShape(BlockState param0) {
        Direction var0 = param0.getValue(FACING);
        BellAttachType var1 = param0.getValue(ATTACHMENT);
        if (var1 == BellAttachType.FLOOR) {
            return var0 != Direction.NORTH && var0 != Direction.SOUTH ? EAST_WEST_FLOOR_SHAPE : NORTH_SOUTH_FLOOR_SHAPE;
        } else if (var1 == BellAttachType.CEILING) {
            return CEILING_SHAPE;
        } else if (var1 == BellAttachType.DOUBLE_WALL) {
            return var0 != Direction.NORTH && var0 != Direction.SOUTH ? EAST_WEST_BETWEEN : NORTH_SOUTH_BETWEEN;
        } else if (var0 == Direction.NORTH) {
            return TO_NORTH;
        } else if (var0 == Direction.SOUTH) {
            return TO_SOUTH;
        } else {
            return var0 == Direction.EAST ? TO_EAST : TO_WEST;
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return this.getVoxelShape(param0);
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return this.getVoxelShape(param0);
    }

    @Override
    public RenderShape getRenderShape(BlockState param0) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        Direction var0 = param0.getClickedFace();
        BlockPos var1 = param0.getClickedPos();
        Level var2 = param0.getLevel();
        Direction.Axis var3 = var0.getAxis();
        if (var3 == Direction.Axis.Y) {
            BlockState var4 = this.defaultBlockState()
                .setValue(ATTACHMENT, var0 == Direction.DOWN ? BellAttachType.CEILING : BellAttachType.FLOOR)
                .setValue(FACING, param0.getHorizontalDirection());
            if (var4.canSurvive(param0.getLevel(), var1)) {
                return var4;
            }
        } else {
            boolean var5 = var3 == Direction.Axis.X
                    && var2.getBlockState(var1.west()).isFaceSturdy(var2, var1.west(), Direction.EAST)
                    && var2.getBlockState(var1.east()).isFaceSturdy(var2, var1.east(), Direction.WEST)
                || var3 == Direction.Axis.Z
                    && var2.getBlockState(var1.north()).isFaceSturdy(var2, var1.north(), Direction.SOUTH)
                    && var2.getBlockState(var1.south()).isFaceSturdy(var2, var1.south(), Direction.NORTH);
            BlockState var6 = this.defaultBlockState()
                .setValue(FACING, var0.getOpposite())
                .setValue(ATTACHMENT, var5 ? BellAttachType.DOUBLE_WALL : BellAttachType.SINGLE_WALL);
            if (var6.canSurvive(param0.getLevel(), param0.getClickedPos())) {
                return var6;
            }

            boolean var7 = var2.getBlockState(var1.below()).isFaceSturdy(var2, var1.below(), Direction.UP);
            var6 = var6.setValue(ATTACHMENT, var7 ? BellAttachType.FLOOR : BellAttachType.CEILING);
            if (var6.canSurvive(param0.getLevel(), param0.getClickedPos())) {
                return var6;
            }
        }

        return null;
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        BellAttachType var0 = param0.getValue(ATTACHMENT);
        Direction var1 = getConnectedDirection(param0).getOpposite();
        if (var1 == param1 && !param0.canSurvive(param3, param4) && var0 != BellAttachType.DOUBLE_WALL) {
            return Blocks.AIR.defaultBlockState();
        } else {
            if (param1.getAxis() == param0.getValue(FACING).getAxis()) {
                if (var0 == BellAttachType.DOUBLE_WALL && !param2.isFaceSturdy(param3, param5, param1)) {
                    return param0.setValue(ATTACHMENT, BellAttachType.SINGLE_WALL).setValue(FACING, param1.getOpposite());
                }

                if (var0 == BellAttachType.SINGLE_WALL && var1.getOpposite() == param1 && param2.isFaceSturdy(param3, param5, param0.getValue(FACING))) {
                    return param0.setValue(ATTACHMENT, BellAttachType.DOUBLE_WALL);
                }
            }

            return super.updateShape(param0, param1, param2, param3, param4, param5);
        }
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        return FaceAttachedHorizontalDirectionalBlock.canAttach(param1, param2, getConnectedDirection(param0).getOpposite());
    }

    private static Direction getConnectedDirection(BlockState param0) {
        switch((BellAttachType)param0.getValue(ATTACHMENT)) {
            case FLOOR:
                return Direction.UP;
            case CEILING:
                return Direction.DOWN;
            default:
                return param0.getValue(FACING).getOpposite();
        }
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState param0) {
        return PushReaction.DESTROY;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(FACING, ATTACHMENT, POWERED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockGetter param0) {
        return new BellBlockEntity();
    }

    @Override
    public boolean isPathfindable(BlockState param0, BlockGetter param1, BlockPos param2, PathComputationType param3) {
        return false;
    }
}

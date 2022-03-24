package net.minecraft.world.level.block;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class ButtonBlock extends FaceAttachedHorizontalDirectionalBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private static final int PRESSED_DEPTH = 1;
    private static final int UNPRESSED_DEPTH = 2;
    protected static final int HALF_AABB_HEIGHT = 2;
    protected static final int HALF_AABB_WIDTH = 3;
    protected static final VoxelShape CEILING_AABB_X = Block.box(6.0, 14.0, 5.0, 10.0, 16.0, 11.0);
    protected static final VoxelShape CEILING_AABB_Z = Block.box(5.0, 14.0, 6.0, 11.0, 16.0, 10.0);
    protected static final VoxelShape FLOOR_AABB_X = Block.box(6.0, 0.0, 5.0, 10.0, 2.0, 11.0);
    protected static final VoxelShape FLOOR_AABB_Z = Block.box(5.0, 0.0, 6.0, 11.0, 2.0, 10.0);
    protected static final VoxelShape NORTH_AABB = Block.box(5.0, 6.0, 14.0, 11.0, 10.0, 16.0);
    protected static final VoxelShape SOUTH_AABB = Block.box(5.0, 6.0, 0.0, 11.0, 10.0, 2.0);
    protected static final VoxelShape WEST_AABB = Block.box(14.0, 6.0, 5.0, 16.0, 10.0, 11.0);
    protected static final VoxelShape EAST_AABB = Block.box(0.0, 6.0, 5.0, 2.0, 10.0, 11.0);
    protected static final VoxelShape PRESSED_CEILING_AABB_X = Block.box(6.0, 15.0, 5.0, 10.0, 16.0, 11.0);
    protected static final VoxelShape PRESSED_CEILING_AABB_Z = Block.box(5.0, 15.0, 6.0, 11.0, 16.0, 10.0);
    protected static final VoxelShape PRESSED_FLOOR_AABB_X = Block.box(6.0, 0.0, 5.0, 10.0, 1.0, 11.0);
    protected static final VoxelShape PRESSED_FLOOR_AABB_Z = Block.box(5.0, 0.0, 6.0, 11.0, 1.0, 10.0);
    protected static final VoxelShape PRESSED_NORTH_AABB = Block.box(5.0, 6.0, 15.0, 11.0, 10.0, 16.0);
    protected static final VoxelShape PRESSED_SOUTH_AABB = Block.box(5.0, 6.0, 0.0, 11.0, 10.0, 1.0);
    protected static final VoxelShape PRESSED_WEST_AABB = Block.box(15.0, 6.0, 5.0, 16.0, 10.0, 11.0);
    protected static final VoxelShape PRESSED_EAST_AABB = Block.box(0.0, 6.0, 5.0, 1.0, 10.0, 11.0);
    private final boolean sensitive;

    protected ButtonBlock(boolean param0, BlockBehaviour.Properties param1) {
        super(param1);
        this.registerDefaultState(
            this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, Boolean.valueOf(false)).setValue(FACE, AttachFace.WALL)
        );
        this.sensitive = param0;
    }

    private int getPressDuration() {
        return this.sensitive ? 30 : 20;
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        Direction var0 = param0.getValue(FACING);
        boolean var1 = param0.getValue(POWERED);
        switch((AttachFace)param0.getValue(FACE)) {
            case FLOOR:
                if (var0.getAxis() == Direction.Axis.X) {
                    return var1 ? PRESSED_FLOOR_AABB_X : FLOOR_AABB_X;
                }

                return var1 ? PRESSED_FLOOR_AABB_Z : FLOOR_AABB_Z;
            case WALL:
                switch(var0) {
                    case EAST:
                        return var1 ? PRESSED_EAST_AABB : EAST_AABB;
                    case WEST:
                        return var1 ? PRESSED_WEST_AABB : WEST_AABB;
                    case SOUTH:
                        return var1 ? PRESSED_SOUTH_AABB : SOUTH_AABB;
                    case NORTH:
                    default:
                        return var1 ? PRESSED_NORTH_AABB : NORTH_AABB;
                }
            case CEILING:
            default:
                if (var0.getAxis() == Direction.Axis.X) {
                    return var1 ? PRESSED_CEILING_AABB_X : CEILING_AABB_X;
                } else {
                    return var1 ? PRESSED_CEILING_AABB_Z : CEILING_AABB_Z;
                }
        }
    }

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        if (param0.getValue(POWERED)) {
            return InteractionResult.CONSUME;
        } else {
            this.press(param0, param1, param2);
            this.playSound(param3, param1, param2, true);
            param1.gameEvent(param3, GameEvent.BLOCK_PRESS, param2);
            return InteractionResult.sidedSuccess(param1.isClientSide);
        }
    }

    public void press(BlockState param0, Level param1, BlockPos param2) {
        param1.setBlock(param2, param0.setValue(POWERED, Boolean.valueOf(true)), 3);
        this.updateNeighbours(param0, param1, param2);
        param1.scheduleTick(param2, this, this.getPressDuration());
    }

    protected void playSound(@Nullable Player param0, LevelAccessor param1, BlockPos param2, boolean param3) {
        param1.playSound(param3 ? param0 : null, param2, this.getSound(param3), SoundSource.BLOCKS, 0.3F, param3 ? 0.6F : 0.5F);
    }

    protected abstract SoundEvent getSound(boolean var1);

    @Override
    public void onRemove(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        if (!param4 && !param0.is(param3.getBlock())) {
            if (param0.getValue(POWERED)) {
                this.updateNeighbours(param0, param1, param2);
            }

            super.onRemove(param0, param1, param2, param3, param4);
        }
    }

    @Override
    public int getSignal(BlockState param0, BlockGetter param1, BlockPos param2, Direction param3) {
        return param0.getValue(POWERED) ? 15 : 0;
    }

    @Override
    public int getDirectSignal(BlockState param0, BlockGetter param1, BlockPos param2, Direction param3) {
        return param0.getValue(POWERED) && getConnectedDirection(param0) == param3 ? 15 : 0;
    }

    @Override
    public boolean isSignalSource(BlockState param0) {
        return true;
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        if (param0.getValue(POWERED)) {
            if (this.sensitive) {
                this.checkPressed(param0, param1, param2);
            } else {
                param1.setBlock(param2, param0.setValue(POWERED, Boolean.valueOf(false)), 3);
                this.updateNeighbours(param0, param1, param2);
                this.playSound(null, param1, param2, false);
                param1.gameEvent(null, GameEvent.BLOCK_UNPRESS, param2);
            }

        }
    }

    @Override
    public void entityInside(BlockState param0, Level param1, BlockPos param2, Entity param3) {
        if (!param1.isClientSide && this.sensitive && !param0.getValue(POWERED)) {
            this.checkPressed(param0, param1, param2);
        }
    }

    private void checkPressed(BlockState param0, Level param1, BlockPos param2) {
        List<? extends Entity> var0 = param1.getEntitiesOfClass(AbstractArrow.class, param0.getShape(param1, param2).bounds().move(param2));
        boolean var1 = !var0.isEmpty();
        boolean var2 = param0.getValue(POWERED);
        if (var1 != var2) {
            param1.setBlock(param2, param0.setValue(POWERED, Boolean.valueOf(var1)), 3);
            this.updateNeighbours(param0, param1, param2);
            this.playSound(null, param1, param2, var1);
            param1.gameEvent(var0.stream().findFirst().orElse(null), var1 ? GameEvent.BLOCK_PRESS : GameEvent.BLOCK_UNPRESS, param2);
        }

        if (var1) {
            param1.scheduleTick(new BlockPos(param2), this, this.getPressDuration());
        }

    }

    private void updateNeighbours(BlockState param0, Level param1, BlockPos param2) {
        param1.updateNeighborsAt(param2, this);
        param1.updateNeighborsAt(param2.relative(getConnectedDirection(param0).getOpposite()), this);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(FACING, POWERED, FACE);
    }
}

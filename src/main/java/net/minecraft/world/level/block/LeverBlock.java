package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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

public class LeverBlock extends FaceAttachedHorizontalDirectionalBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    protected static final int DEPTH = 6;
    protected static final int WIDTH = 6;
    protected static final int HEIGHT = 8;
    protected static final VoxelShape NORTH_AABB = Block.box(5.0, 4.0, 10.0, 11.0, 12.0, 16.0);
    protected static final VoxelShape SOUTH_AABB = Block.box(5.0, 4.0, 0.0, 11.0, 12.0, 6.0);
    protected static final VoxelShape WEST_AABB = Block.box(10.0, 4.0, 5.0, 16.0, 12.0, 11.0);
    protected static final VoxelShape EAST_AABB = Block.box(0.0, 4.0, 5.0, 6.0, 12.0, 11.0);
    protected static final VoxelShape UP_AABB_Z = Block.box(5.0, 0.0, 4.0, 11.0, 6.0, 12.0);
    protected static final VoxelShape UP_AABB_X = Block.box(4.0, 0.0, 5.0, 12.0, 6.0, 11.0);
    protected static final VoxelShape DOWN_AABB_Z = Block.box(5.0, 10.0, 4.0, 11.0, 16.0, 12.0);
    protected static final VoxelShape DOWN_AABB_X = Block.box(4.0, 10.0, 5.0, 12.0, 16.0, 11.0);

    protected LeverBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(
            this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, Boolean.valueOf(false)).setValue(FACE, AttachFace.WALL)
        );
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        switch((AttachFace)param0.getValue(FACE)) {
            case FLOOR:
                switch(param0.getValue(FACING).getAxis()) {
                    case X:
                        return UP_AABB_X;
                    case Z:
                    default:
                        return UP_AABB_Z;
                }
            case WALL:
                switch((Direction)param0.getValue(FACING)) {
                    case EAST:
                        return EAST_AABB;
                    case WEST:
                        return WEST_AABB;
                    case SOUTH:
                        return SOUTH_AABB;
                    case NORTH:
                    default:
                        return NORTH_AABB;
                }
            case CEILING:
            default:
                switch(param0.getValue(FACING).getAxis()) {
                    case X:
                        return DOWN_AABB_X;
                    case Z:
                    default:
                        return DOWN_AABB_Z;
                }
        }
    }

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        if (param1.isClientSide) {
            BlockState var0 = param0.cycle(POWERED);
            if (var0.getValue(POWERED)) {
                makeParticle(var0, param1, param2, 1.0F);
            }

            return InteractionResult.SUCCESS;
        } else {
            BlockState var1 = this.pull(param0, param1, param2);
            float var2 = var1.getValue(POWERED) ? 0.6F : 0.5F;
            param1.playSound(null, param2, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, var2);
            param1.gameEvent(param3, var1.getValue(POWERED) ? GameEvent.BLOCK_ACTIVATE : GameEvent.BLOCK_DEACTIVATE, param2);
            return InteractionResult.CONSUME;
        }
    }

    public BlockState pull(BlockState param0, Level param1, BlockPos param2) {
        param0 = param0.cycle(POWERED);
        param1.setBlock(param2, param0, 3);
        this.updateNeighbours(param0, param1, param2);
        return param0;
    }

    private static void makeParticle(BlockState param0, LevelAccessor param1, BlockPos param2, float param3) {
        Direction var0 = param0.getValue(FACING).getOpposite();
        Direction var1 = getConnectedDirection(param0).getOpposite();
        double var2 = (double)param2.getX() + 0.5 + 0.1 * (double)var0.getStepX() + 0.2 * (double)var1.getStepX();
        double var3 = (double)param2.getY() + 0.5 + 0.1 * (double)var0.getStepY() + 0.2 * (double)var1.getStepY();
        double var4 = (double)param2.getZ() + 0.5 + 0.1 * (double)var0.getStepZ() + 0.2 * (double)var1.getStepZ();
        param1.addParticle(new DustParticleOptions(DustParticleOptions.REDSTONE_PARTICLE_COLOR, param3), var2, var3, var4, 0.0, 0.0, 0.0);
    }

    @Override
    public void animateTick(BlockState param0, Level param1, BlockPos param2, Random param3) {
        if (param0.getValue(POWERED) && param3.nextFloat() < 0.25F) {
            makeParticle(param0, param1, param2, 0.5F);
        }

    }

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

    private void updateNeighbours(BlockState param0, Level param1, BlockPos param2) {
        param1.updateNeighborsAt(param2, this);
        param1.updateNeighborsAt(param2.relative(getConnectedDirection(param0).getOpposite()), this);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(FACE, FACING, POWERED);
    }
}

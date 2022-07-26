package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SculkSensorBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.SculkSensorPhase;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SculkSensorBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final int ACTIVE_TICKS = 40;
    public static final int COOLDOWN_TICKS = 1;
    public static final EnumProperty<SculkSensorPhase> PHASE = BlockStateProperties.SCULK_SENSOR_PHASE;
    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
    private final int listenerRange;

    public SculkSensorBlock(BlockBehaviour.Properties param0, int param1) {
        super(param0);
        this.registerDefaultState(
            this.stateDefinition
                .any()
                .setValue(PHASE, SculkSensorPhase.INACTIVE)
                .setValue(POWER, Integer.valueOf(0))
                .setValue(WATERLOGGED, Boolean.valueOf(false))
        );
        this.listenerRange = param1;
    }

    public int getListenerRange() {
        return this.listenerRange;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        BlockPos var0 = param0.getClickedPos();
        FluidState var1 = param0.getLevel().getFluidState(var0);
        return this.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(var1.getType() == Fluids.WATER));
    }

    @Override
    public FluidState getFluidState(BlockState param0) {
        return param0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(param0);
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, RandomSource param3) {
        if (getPhase(param0) != SculkSensorPhase.ACTIVE) {
            if (getPhase(param0) == SculkSensorPhase.COOLDOWN) {
                param1.setBlock(param2, param0.setValue(PHASE, SculkSensorPhase.INACTIVE), 3);
            }

        } else {
            deactivate(param1, param2, param0);
        }
    }

    @Override
    public void stepOn(Level param0, BlockPos param1, BlockState param2, Entity param3) {
        if (!param0.isClientSide() && canActivate(param2) && param3.getType() != EntityType.WARDEN) {
            BlockEntity var0 = param0.getBlockEntity(param1);
            if (var0 instanceof SculkSensorBlockEntity var1 && param0 instanceof ServerLevel var2) {
                var1.getListener().forceGameEvent(var2, GameEvent.STEP, GameEvent.Context.of(param3), param3.position());
            }
        }

        super.stepOn(param0, param1, param2, param3);
    }

    @Override
    public void onPlace(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        if (!param1.isClientSide() && !param0.is(param3.getBlock())) {
            if (param0.getValue(POWER) > 0 && !param1.getBlockTicks().hasScheduledTick(param2, this)) {
                param1.setBlock(param2, param0.setValue(POWER, Integer.valueOf(0)), 18);
            }

            param1.scheduleTick(new BlockPos(param2), param0.getBlock(), 1);
        }
    }

    @Override
    public void onRemove(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        if (!param0.is(param3.getBlock())) {
            if (getPhase(param0) == SculkSensorPhase.ACTIVE) {
                updateNeighbours(param1, param2);
            }

            super.onRemove(param0, param1, param2, param3, param4);
        }
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param0.getValue(WATERLOGGED)) {
            param3.scheduleTick(param4, Fluids.WATER, Fluids.WATER.getTickDelay(param3));
        }

        return super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    private static void updateNeighbours(Level param0, BlockPos param1) {
        param0.updateNeighborsAt(param1, Blocks.SCULK_SENSOR);
        param0.updateNeighborsAt(param1.relative(Direction.UP.getOpposite()), Blocks.SCULK_SENSOR);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos param0, BlockState param1) {
        return new SculkSensorBlockEntity(param0, param1);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> GameEventListener getListener(ServerLevel param0, T param1) {
        return param1 instanceof SculkSensorBlockEntity ? ((SculkSensorBlockEntity)param1).getListener() : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level param0, BlockState param1, BlockEntityType<T> param2) {
        return !param0.isClientSide
            ? createTickerHelper(param2, BlockEntityType.SCULK_SENSOR, (param0x, param1x, param2x, param3) -> param3.getListener().tick(param0x))
            : null;
    }

    @Override
    public RenderShape getRenderShape(BlockState param0) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPE;
    }

    @Override
    public boolean isSignalSource(BlockState param0) {
        return true;
    }

    @Override
    public int getSignal(BlockState param0, BlockGetter param1, BlockPos param2, Direction param3) {
        return param0.getValue(POWER);
    }

    public static SculkSensorPhase getPhase(BlockState param0) {
        return param0.getValue(PHASE);
    }

    public static boolean canActivate(BlockState param0) {
        return getPhase(param0) == SculkSensorPhase.INACTIVE;
    }

    public static void deactivate(Level param0, BlockPos param1, BlockState param2) {
        param0.setBlock(param1, param2.setValue(PHASE, SculkSensorPhase.COOLDOWN).setValue(POWER, Integer.valueOf(0)), 3);
        param0.scheduleTick(param1, param2.getBlock(), 1);
        if (!param2.getValue(WATERLOGGED)) {
            param0.playSound(null, param1, SoundEvents.SCULK_CLICKING_STOP, SoundSource.BLOCKS, 1.0F, param0.random.nextFloat() * 0.2F + 0.8F);
        }

        updateNeighbours(param0, param1);
    }

    public static void activate(@Nullable Entity param0, Level param1, BlockPos param2, BlockState param3, int param4) {
        param1.setBlock(param2, param3.setValue(PHASE, SculkSensorPhase.ACTIVE).setValue(POWER, Integer.valueOf(param4)), 3);
        param1.scheduleTick(param2, param3.getBlock(), 40);
        updateNeighbours(param1, param2);
        param1.gameEvent(param0, GameEvent.SCULK_SENSOR_TENDRILS_CLICKING, param2);
        if (!param3.getValue(WATERLOGGED)) {
            param1.playSound(
                null,
                (double)param2.getX() + 0.5,
                (double)param2.getY() + 0.5,
                (double)param2.getZ() + 0.5,
                SoundEvents.SCULK_CLICKING,
                SoundSource.BLOCKS,
                1.0F,
                param1.random.nextFloat() * 0.2F + 0.8F
            );
        }

    }

    @Override
    public void animateTick(BlockState param0, Level param1, BlockPos param2, RandomSource param3) {
        if (getPhase(param0) == SculkSensorPhase.ACTIVE) {
            Direction var0 = Direction.getRandom(param3);
            if (var0 != Direction.UP && var0 != Direction.DOWN) {
                double var1 = (double)param2.getX() + 0.5 + (var0.getStepX() == 0 ? 0.5 - param3.nextDouble() : (double)var0.getStepX() * 0.6);
                double var2 = (double)param2.getY() + 0.25;
                double var3 = (double)param2.getZ() + 0.5 + (var0.getStepZ() == 0 ? 0.5 - param3.nextDouble() : (double)var0.getStepZ() * 0.6);
                double var4 = (double)param3.nextFloat() * 0.04;
                param1.addParticle(DustColorTransitionOptions.SCULK_TO_REDSTONE, var1, var2, var3, 0.0, var4, 0.0);
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(PHASE, POWER, WATERLOGGED);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState param0) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState param0, Level param1, BlockPos param2) {
        BlockEntity var0 = param1.getBlockEntity(param2);
        if (var0 instanceof SculkSensorBlockEntity var1) {
            return getPhase(param0) == SculkSensorPhase.ACTIVE ? var1.getLastVibrationFrequency() : 0;
        } else {
            return 0;
        }
    }

    @Override
    public boolean isPathfindable(BlockState param0, BlockGetter param1, BlockPos param2, PathComputationType param3) {
        return false;
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState param0) {
        return true;
    }

    @Override
    public void spawnAfterBreak(BlockState param0, ServerLevel param1, BlockPos param2, ItemStack param3, boolean param4) {
        super.spawnAfterBreak(param0, param1, param2, param3, param4);
        if (param4) {
            this.tryDropExperience(param1, param2, param3, ConstantInt.of(5));
        }

    }
}

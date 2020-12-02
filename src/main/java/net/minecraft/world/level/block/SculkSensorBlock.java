package net.minecraft.world.level.block;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SculkSensorBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final Object2IntMap<GameEvent> VIBRATION_STRENGTH_FOR_EVENT = Object2IntMaps.unmodifiable(
        Util.make(new Object2IntOpenHashMap<>(), param0 -> {
            param0.put(GameEvent.STEP, 1);
            param0.put(GameEvent.FLAP, 2);
            param0.put(GameEvent.SWIM, 3);
            param0.put(GameEvent.ELYTRA_FREE_FALL, 4);
            param0.put(GameEvent.HIT_GROUND, 5);
            param0.put(GameEvent.SPLASH, 6);
            param0.put(GameEvent.WOLF_SHAKING, 6);
            param0.put(GameEvent.PROJECTILE_SHOOT, 7);
            param0.put(GameEvent.PROJECTILE_LAND, 8);
            param0.put(GameEvent.EATING_START, 7);
            param0.put(GameEvent.EATING_FINISH, 8);
            param0.put(GameEvent.ENTITY_HIT, 9);
            param0.put(GameEvent.ARMOR_STAND_ADD_ITEM, 9);
            param0.put(GameEvent.BLOCK_OPEN, 11);
            param0.put(GameEvent.BLOCK_CLOSE, 10);
            param0.put(GameEvent.BLOCK_SWITCH, 11);
            param0.put(GameEvent.BLOCK_UNSWITCH, 10);
            param0.put(GameEvent.BLOCK_PRESS, 11);
            param0.put(GameEvent.BLOCK_UNPRESS, 10);
            param0.put(GameEvent.BLOCK_ATTACH, 11);
            param0.put(GameEvent.BLOCK_DETACH, 10);
            param0.put(GameEvent.CONTAINER_OPEN, 11);
            param0.put(GameEvent.CONTAINER_CLOSE, 10);
            param0.put(GameEvent.DISPENSE_FAIL, 10);
            param0.put(GameEvent.FLINT_AND_STEEL_USE, 12);
            param0.put(GameEvent.BLOCK_PLACE, 12);
            param0.put(GameEvent.BLOCK_DESTROY, 13);
            param0.put(GameEvent.FLUID_PLACE, 12);
            param0.put(GameEvent.FLUID_PICKUP, 13);
            param0.put(GameEvent.FISHING_ROD_CAST, 15);
            param0.put(GameEvent.FISHING_ROD_REEL_IN, 14);
            param0.put(GameEvent.PISTON_EXTEND, 15);
            param0.put(GameEvent.PISTON_CONTRACT, 14);
            param0.put(GameEvent.EXPLODE, 15);
            param0.put(GameEvent.LIGHTNING_STRIKE, 15);
        })
    );
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
                .setValue(PHASE, SculkSensorPhase.COOLDOWN)
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
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        if (getPhase(param0) != SculkSensorPhase.ACTIVE) {
            if (getPhase(param0) == SculkSensorPhase.COOLDOWN) {
                param1.setBlock(param2, param0.setValue(PHASE, SculkSensorPhase.INACTIVE), 3);
            }

        } else {
            deactivate(param1, param2, param0);
        }
    }

    @Override
    public void onPlace(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        if (!param1.isClientSide() && !param0.is(param3.getBlock())) {
            if (param0.getValue(POWER) > 0 && !param1.getBlockTicks().hasScheduledTick(param2, this)) {
                param1.setBlock(param2, param0.setValue(POWER, Integer.valueOf(0)), 18);
            }

            param1.getBlockTicks().scheduleTick(new BlockPos(param2), param0.getBlock(), 1);
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
            param3.getLiquidTicks().scheduleTick(param4, Fluids.WATER, Fluids.WATER.getTickDelay(param3));
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
    public <T extends BlockEntity> GameEventListener getListener(Level param0, T param1) {
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
        param0.getBlockTicks().scheduleTick(new BlockPos(param1), param2.getBlock(), 1);
        param0.playSound(null, param1, SoundEvents.SCULK_CLICKING_STOP, SoundSource.BLOCKS, 1.0F, param0.random.nextFloat() * 0.2F + 0.8F);
        updateNeighbours(param0, param1);
    }

    public static void activate(Level param0, BlockPos param1, BlockState param2, int param3) {
        param0.setBlock(param1, param2.setValue(PHASE, SculkSensorPhase.ACTIVE).setValue(POWER, Integer.valueOf(param3)), 3);
        param0.getBlockTicks().scheduleTick(new BlockPos(param1), param2.getBlock(), 40);
        updateNeighbours(param0, param1);
        param0.playSound(
            null,
            (double)param1.getX() + 0.5,
            (double)param1.getY() + 0.5,
            (double)param1.getZ() + 0.5,
            SoundEvents.SCULK_CLICKING,
            SoundSource.BLOCKS,
            1.0F,
            param0.random.nextFloat() * 0.2F + 0.8F
        );
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(BlockState param0, Level param1, BlockPos param2, Random param3) {
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
        if (var0 instanceof SculkSensorBlockEntity) {
            SculkSensorBlockEntity var1 = (SculkSensorBlockEntity)var0;
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
}

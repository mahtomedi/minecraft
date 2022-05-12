package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Tilt;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BigDripleafBlock extends HorizontalDirectionalBlock implements BonemealableBlock, SimpleWaterloggedBlock {
    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final EnumProperty<Tilt> TILT = BlockStateProperties.TILT;
    private static final int NO_TICK = -1;
    private static final Object2IntMap<Tilt> DELAY_UNTIL_NEXT_TILT_STATE = Util.make(new Object2IntArrayMap<>(), param0 -> {
        param0.defaultReturnValue(-1);
        param0.put(Tilt.UNSTABLE, 10);
        param0.put(Tilt.PARTIAL, 10);
        param0.put(Tilt.FULL, 100);
    });
    private static final int MAX_GEN_HEIGHT = 5;
    private static final int STEM_WIDTH = 6;
    private static final int ENTITY_DETECTION_MIN_Y = 11;
    private static final int LOWEST_LEAF_TOP = 13;
    private static final Map<Tilt, VoxelShape> LEAF_SHAPES = ImmutableMap.of(
        Tilt.NONE,
        Block.box(0.0, 11.0, 0.0, 16.0, 15.0, 16.0),
        Tilt.UNSTABLE,
        Block.box(0.0, 11.0, 0.0, 16.0, 15.0, 16.0),
        Tilt.PARTIAL,
        Block.box(0.0, 11.0, 0.0, 16.0, 13.0, 16.0),
        Tilt.FULL,
        Shapes.empty()
    );
    private static final VoxelShape STEM_SLICER = Block.box(0.0, 13.0, 0.0, 16.0, 16.0, 16.0);
    private static final Map<Direction, VoxelShape> STEM_SHAPES = ImmutableMap.of(
        Direction.NORTH,
        Shapes.joinUnoptimized(BigDripleafStemBlock.NORTH_SHAPE, STEM_SLICER, BooleanOp.ONLY_FIRST),
        Direction.SOUTH,
        Shapes.joinUnoptimized(BigDripleafStemBlock.SOUTH_SHAPE, STEM_SLICER, BooleanOp.ONLY_FIRST),
        Direction.EAST,
        Shapes.joinUnoptimized(BigDripleafStemBlock.EAST_SHAPE, STEM_SLICER, BooleanOp.ONLY_FIRST),
        Direction.WEST,
        Shapes.joinUnoptimized(BigDripleafStemBlock.WEST_SHAPE, STEM_SLICER, BooleanOp.ONLY_FIRST)
    );
    private final Map<BlockState, VoxelShape> shapesCache;

    protected BigDripleafBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(
            this.stateDefinition.any().setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(FACING, Direction.NORTH).setValue(TILT, Tilt.NONE)
        );
        this.shapesCache = this.getShapeForEachState(BigDripleafBlock::calculateShape);
    }

    private static VoxelShape calculateShape(BlockState param0x) {
        return Shapes.or(LEAF_SHAPES.get(param0x.getValue(TILT)), STEM_SHAPES.get(param0x.getValue(FACING)));
    }

    public static void placeWithRandomHeight(LevelAccessor param0, RandomSource param1, BlockPos param2, Direction param3) {
        int var0 = Mth.nextInt(param1, 2, 5);
        BlockPos.MutableBlockPos var1 = param2.mutable();
        int var2 = 0;

        while(var2 < var0 && canPlaceAt(param0, var1, param0.getBlockState(var1))) {
            ++var2;
            var1.move(Direction.UP);
        }

        int var3 = param2.getY() + var2 - 1;
        var1.setY(param2.getY());

        while(var1.getY() < var3) {
            BigDripleafStemBlock.place(param0, var1, param0.getFluidState(var1), param3);
            var1.move(Direction.UP);
        }

        place(param0, var1, param0.getFluidState(var1), param3);
    }

    private static boolean canReplace(BlockState param0) {
        return param0.isAir() || param0.is(Blocks.WATER) || param0.is(Blocks.SMALL_DRIPLEAF);
    }

    protected static boolean canPlaceAt(LevelHeightAccessor param0, BlockPos param1, BlockState param2) {
        return !param0.isOutsideBuildHeight(param1) && canReplace(param2);
    }

    protected static boolean place(LevelAccessor param0, BlockPos param1, FluidState param2, Direction param3) {
        BlockState var0 = Blocks.BIG_DRIPLEAF
            .defaultBlockState()
            .setValue(WATERLOGGED, Boolean.valueOf(param2.isSourceOfType(Fluids.WATER)))
            .setValue(FACING, param3);
        return param0.setBlock(param1, var0, 3);
    }

    @Override
    public void onProjectileHit(Level param0, BlockState param1, BlockHitResult param2, Projectile param3) {
        this.setTiltAndScheduleTick(param1, param0, param2.getBlockPos(), Tilt.FULL, SoundEvents.BIG_DRIPLEAF_TILT_DOWN);
    }

    @Override
    public FluidState getFluidState(BlockState param0) {
        return param0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(param0);
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        BlockPos var0 = param2.below();
        BlockState var1 = param1.getBlockState(var0);
        return var1.is(this) || var1.is(Blocks.BIG_DRIPLEAF_STEM) || var1.is(BlockTags.BIG_DRIPLEAF_PLACEABLE);
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param1 == Direction.DOWN && !param0.canSurvive(param3, param4)) {
            return Blocks.AIR.defaultBlockState();
        } else {
            if (param0.getValue(WATERLOGGED)) {
                param3.scheduleTick(param4, Fluids.WATER, Fluids.WATER.getTickDelay(param3));
            }

            return param1 == Direction.UP && param2.is(this)
                ? Blocks.BIG_DRIPLEAF_STEM.withPropertiesOf(param0)
                : super.updateShape(param0, param1, param2, param3, param4, param5);
        }
    }

    @Override
    public boolean isValidBonemealTarget(BlockGetter param0, BlockPos param1, BlockState param2, boolean param3) {
        BlockState var0 = param0.getBlockState(param1.above());
        return canReplace(var0);
    }

    @Override
    public boolean isBonemealSuccess(Level param0, RandomSource param1, BlockPos param2, BlockState param3) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel param0, RandomSource param1, BlockPos param2, BlockState param3) {
        BlockPos var0 = param2.above();
        BlockState var1 = param0.getBlockState(var0);
        if (canPlaceAt(param0, var0, var1)) {
            Direction var2 = param3.getValue(FACING);
            BigDripleafStemBlock.place(param0, param2, param3.getFluidState(), var2);
            place(param0, var0, var1.getFluidState(), var2);
        }

    }

    @Override
    public void entityInside(BlockState param0, Level param1, BlockPos param2, Entity param3) {
        if (!param1.isClientSide) {
            if (param0.getValue(TILT) == Tilt.NONE && canEntityTilt(param2, param3) && !param1.hasNeighborSignal(param2)) {
                this.setTiltAndScheduleTick(param0, param1, param2, Tilt.UNSTABLE, null);
            }

        }
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, RandomSource param3) {
        if (param1.hasNeighborSignal(param2)) {
            resetTilt(param0, param1, param2);
        } else {
            Tilt var0 = param0.getValue(TILT);
            if (var0 == Tilt.UNSTABLE) {
                this.setTiltAndScheduleTick(param0, param1, param2, Tilt.PARTIAL, SoundEvents.BIG_DRIPLEAF_TILT_DOWN);
            } else if (var0 == Tilt.PARTIAL) {
                this.setTiltAndScheduleTick(param0, param1, param2, Tilt.FULL, SoundEvents.BIG_DRIPLEAF_TILT_DOWN);
            } else if (var0 == Tilt.FULL) {
                resetTilt(param0, param1, param2);
            }

        }
    }

    @Override
    public void neighborChanged(BlockState param0, Level param1, BlockPos param2, Block param3, BlockPos param4, boolean param5) {
        if (param1.hasNeighborSignal(param2)) {
            resetTilt(param0, param1, param2);
        }

    }

    private static void playTiltSound(Level param0, BlockPos param1, SoundEvent param2) {
        float var0 = Mth.randomBetween(param0.random, 0.8F, 1.2F);
        param0.playSound(null, param1, param2, SoundSource.BLOCKS, 1.0F, var0);
    }

    private static boolean canEntityTilt(BlockPos param0, Entity param1) {
        return param1.isOnGround() && param1.position().y > (double)((float)param0.getY() + 0.6875F);
    }

    private void setTiltAndScheduleTick(BlockState param0, Level param1, BlockPos param2, Tilt param3, @Nullable SoundEvent param4) {
        setTilt(param0, param1, param2, param3);
        if (param4 != null) {
            playTiltSound(param1, param2, param4);
        }

        int var0 = DELAY_UNTIL_NEXT_TILT_STATE.getInt(param3);
        if (var0 != -1) {
            param1.scheduleTick(param2, this, var0);
        }

    }

    private static void resetTilt(BlockState param0, Level param1, BlockPos param2) {
        setTilt(param0, param1, param2, Tilt.NONE);
        if (param0.getValue(TILT) != Tilt.NONE) {
            playTiltSound(param1, param2, SoundEvents.BIG_DRIPLEAF_TILT_UP);
        }

    }

    private static void setTilt(BlockState param0, Level param1, BlockPos param2, Tilt param3) {
        Tilt var0 = param0.getValue(TILT);
        param1.setBlock(param2, param0.setValue(TILT, param3), 2);
        if (param3.causesVibration() && param3 != var0) {
            param1.gameEvent(null, GameEvent.BLOCK_CHANGE, param2);
        }

    }

    @Override
    public VoxelShape getCollisionShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return LEAF_SHAPES.get(param0.getValue(TILT));
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return this.shapesCache.get(param0);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        BlockState var0 = param0.getLevel().getBlockState(param0.getClickedPos().below());
        FluidState var1 = param0.getLevel().getFluidState(param0.getClickedPos());
        boolean var2 = var0.is(Blocks.BIG_DRIPLEAF) || var0.is(Blocks.BIG_DRIPLEAF_STEM);
        return this.defaultBlockState()
            .setValue(WATERLOGGED, Boolean.valueOf(var1.isSourceOfType(Fluids.WATER)))
            .setValue(FACING, var2 ? var0.getValue(FACING) : param0.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(WATERLOGGED, FACING, TILT);
    }
}

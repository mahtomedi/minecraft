package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Tilt;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BigDripleafBlock extends HorizontalDirectionalBlock implements BonemealableBlock, SimpleWaterloggedBlock {
    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final EnumProperty<Tilt> TILT = BlockStateProperties.TILT;
    private static final Object2IntMap<Tilt> DELAY_UNTIL_NEXT_TILT_STATE = Util.make(new Object2IntArrayMap<>(), param0 -> {
        param0.defaultReturnValue(-1);
        param0.put(Tilt.UNSTABLE, 20);
        param0.put(Tilt.PARTIAL, 10);
        param0.put(Tilt.FULL, 100);
    });
    private static final AABB ENTITY_DETECTION_SHAPE = Block.box(0.0, 11.0, 0.0, 16.0, 16.0, 16.0).bounds();
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
    private static final Map<Direction, VoxelShape> STEM_SHAPES = ImmutableMap.of(
        Direction.NORTH,
        Block.box(5.0, 0.0, 8.0, 11.0, 11.0, 14.0),
        Direction.SOUTH,
        Block.box(5.0, 0.0, 2.0, 11.0, 11.0, 8.0),
        Direction.EAST,
        Block.box(2.0, 0.0, 5.0, 8.0, 11.0, 11.0),
        Direction.WEST,
        Block.box(8.0, 0.0, 5.0, 14.0, 11.0, 11.0)
    );
    private final Map<BlockState, VoxelShape> shapesCache;

    protected BigDripleafBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(
            this.stateDefinition.any().setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(FACING, Direction.NORTH).setValue(TILT, Tilt.NONE)
        );
        this.shapesCache = this.getShapeForEachState(BigDripleafBlock::calculateShape);
    }

    protected static VoxelShape calculateShape(BlockState param0x) {
        return Shapes.or(getLeafShape(param0x), getStemShape(param0x));
    }

    private static VoxelShape getStemShape(BlockState param0) {
        return STEM_SHAPES.get(param0.getValue(FACING));
    }

    private static VoxelShape getLeafShape(BlockState param0) {
        return LEAF_SHAPES.get(param0.getValue(TILT));
    }

    protected static void place(Level param0, Random param1, BlockPos param2) {
        int var0 = param0.getMaxBuildHeight() - param2.getY();
        int var1 = 1 + param1.nextInt(5);
        int var2 = Math.min(var1, var0);
        Direction var3 = Direction.Plane.HORIZONTAL.getRandomDirection(param1);
        BlockPos.MutableBlockPos var4 = param2.mutable();

        for(int var5 = 0; var5 < var2; ++var5) {
            Block var6 = var5 == var2 - 1 ? Blocks.BIG_DRIPLEAF : Blocks.BIG_DRIPLEAF_STEM;
            BlockState var7 = var6.defaultBlockState()
                .setValue(WATERLOGGED, Boolean.valueOf(param0.getFluidState(var4).getType() == Fluids.WATER))
                .setValue(HorizontalDirectionalBlock.FACING, var3);
            param0.setBlock(var4, var7, 2);
            var4.move(Direction.UP);
        }

    }

    @Override
    public void onProjectileHit(Level param0, BlockState param1, BlockHitResult param2, Projectile param3) {
        param0.destroyBlock(param2.getBlockPos(), true, param3);
    }

    @Override
    public FluidState getFluidState(BlockState param0) {
        return param0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(param0);
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        BlockPos var0 = param2.below();
        BlockState var1 = param1.getBlockState(var0);
        return var1.is(Blocks.BIG_DRIPLEAF_STEM) || var1.isFaceSturdy(param1, var0, Direction.UP);
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (!param0.canSurvive(param3, param4)) {
            param3.destroyBlock(param4, true);
        }

        if (param0.getValue(WATERLOGGED)) {
            param3.getLiquidTicks().scheduleTick(param4, Fluids.WATER, Fluids.WATER.getTickDelay(param3));
        }

        return super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public boolean isValidBonemealTarget(BlockGetter param0, BlockPos param1, BlockState param2, boolean param3) {
        BlockState var0 = param0.getBlockState(param1.above());
        return var0.isAir() || var0.getFluidState().is(FluidTags.WATER);
    }

    @Override
    public boolean isBonemealSuccess(Level param0, Random param1, BlockPos param2, BlockState param3) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel param0, Random param1, BlockPos param2, BlockState param3) {
        BlockPos var0 = param2.above();
        if (param0.isInWorldBounds(var0)) {
            BlockState var1 = param0.getBlockState(var0);
            Fluid var2 = var1.getFluidState().getType();
            boolean var4;
            if (!var1.isAir() && var2 != Fluids.FLOWING_WATER) {
                if (var2 != Fluids.WATER) {
                    return;
                }

                var4 = true;
            } else {
                var4 = false;
            }

            param0.setBlock(
                var0, Blocks.BIG_DRIPLEAF.defaultBlockState().setValue(FACING, param3.getValue(FACING)).setValue(WATERLOGGED, Boolean.valueOf(var4)), 2
            );
            param0.setBlock(
                param2,
                Blocks.BIG_DRIPLEAF_STEM.defaultBlockState().setValue(FACING, param3.getValue(FACING)).setValue(WATERLOGGED, param3.getValue(WATERLOGGED)),
                2
            );
        }
    }

    @Override
    public void entityInside(BlockState param0, Level param1, BlockPos param2, Entity param3) {
        if (!param1.isClientSide) {
            if (param0.getValue(TILT) == Tilt.NONE && canEntityTilt(param2, param3, true)) {
                this.setTiltAndScheduleTick(param0, param1, param2, Tilt.UNSTABLE, null);
            }

        }
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        Tilt var0 = param0.getValue(TILT);
        if (var0 == Tilt.UNSTABLE) {
            if (isAnyEntityTouching(param1, param2, true)) {
                this.setTiltAndScheduleTick(param0, param1, param2, Tilt.PARTIAL, SoundEvents.BIG_DRIPLEAF_TILT_DOWN);
            } else {
                this.resetTilt(param0, param1, param2);
            }
        } else if (var0 == Tilt.PARTIAL) {
            if (isAnyEntityTouching(param1, param2, false)) {
                this.setTiltAndScheduleTick(param0, param1, param2, Tilt.FULL, SoundEvents.BIG_DRIPLEAF_TILT_DOWN);
            } else {
                this.setTiltAndScheduleTick(param0, param1, param2, Tilt.UNSTABLE, SoundEvents.BIG_DRIPLEAF_TILT_UP);
            }
        } else if (var0 == Tilt.FULL) {
            this.resetTilt(param0, param1, param2);
        }

    }

    private void playTiltSound(Level param0, BlockPos param1, SoundEvent param2) {
        float var0 = Mth.randomBetween(param0.random, 0.8F, 1.2F);
        param0.playSound(null, param1, param2, SoundSource.BLOCKS, 1.0F, var0);
    }

    private static boolean isAnyEntityTouching(Level param0, BlockPos param1, boolean param2) {
        Predicate<Entity> var0 = EntitySelector.NO_SPECTATORS.and(param2x -> canEntityTilt(param1, param2x, param2));
        return !param0.getEntities((Entity)null, ENTITY_DETECTION_SHAPE.move(param1), var0).isEmpty();
    }

    private static boolean canEntityTilt(BlockPos param0, Entity param1, boolean param2) {
        return param2 && param1.isSteppingCarefully() ? false : isEntityAbove(param0, param1);
    }

    private static boolean isEntityAbove(BlockPos param0, Entity param1) {
        return param1.position().y > (double)((float)param0.getY() + 0.6875F);
    }

    private void setTiltAndScheduleTick(BlockState param0, Level param1, BlockPos param2, Tilt param3, @Nullable SoundEvent param4) {
        this.setTilt(param0, param1, param2, param3);
        if (param4 != null) {
            this.playTiltSound(param1, param2, param4);
        }

        int var0 = DELAY_UNTIL_NEXT_TILT_STATE.getInt(param3);
        if (var0 != -1) {
            param1.getBlockTicks().scheduleTick(param2, this, var0);
        }

    }

    private void resetTilt(BlockState param0, Level param1, BlockPos param2) {
        this.setTilt(param0, param1, param2, Tilt.NONE);
        this.playTiltSound(param1, param2, SoundEvents.BIG_DRIPLEAF_TILT_UP);
    }

    private void setTilt(BlockState param0, Level param1, BlockPos param2, Tilt param3) {
        param1.setBlock(param2, param0.setValue(TILT, param3), 2);
        if (param3.causesVibration()) {
            param1.gameEvent(GameEvent.BLOCK_CHANGE, param2);
        }

    }

    @Override
    public VoxelShape getCollisionShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return getLeafShape(param0);
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return this.shapesCache.get(param0);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        FluidState var0 = param0.getLevel().getFluidState(param0.getClickedPos());
        return this.defaultBlockState()
            .setValue(WATERLOGGED, Boolean.valueOf(var0.isSourceOfType(Fluids.WATER)))
            .setValue(FACING, param0.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(WATERLOGGED, FACING, TILT);
    }
}

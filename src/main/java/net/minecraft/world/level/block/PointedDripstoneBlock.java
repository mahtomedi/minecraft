package net.minecraft.world.level.block;

import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PointedDripstoneBlock extends Block implements Fallable, SimpleWaterloggedBlock {
    public static final DirectionProperty TIP_DIRECTION = BlockStateProperties.VERTICAL_DIRECTION;
    public static final EnumProperty<DripstoneThickness> THICKNESS = BlockStateProperties.DRIPSTONE_THICKNESS;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final int MAX_SEARCH_LENGTH_WHEN_CHECKING_DRIP_TYPE = 11;
    private static final int DELAY_BEFORE_FALLING = 2;
    private static final float DRIP_PROBABILITY_PER_ANIMATE_TICK = 0.02F;
    private static final float DRIP_PROBABILITY_PER_ANIMATE_TICK_IF_UNDER_LIQUID_SOURCE = 0.12F;
    private static final int MAX_SEARCH_LENGTH_BETWEEN_STALACTITE_TIP_AND_CAULDRON = 11;
    private static final float WATER_TRANSFER_PROBABILITY_PER_RANDOM_TICK = 0.17578125F;
    private static final float LAVA_TRANSFER_PROBABILITY_PER_RANDOM_TICK = 0.05859375F;
    private static final double MIN_TRIDENT_VELOCITY_TO_BREAK_DRIPSTONE = 0.6;
    private static final float STALACTITE_DAMAGE_PER_FALL_DISTANCE_AND_SIZE = 1.0F;
    private static final int STALACTITE_MAX_DAMAGE = 40;
    private static final int MAX_STALACTITE_HEIGHT_FOR_DAMAGE_CALCULATION = 6;
    private static final float STALAGMITE_FALL_DISTANCE_OFFSET = 2.0F;
    private static final int STALAGMITE_FALL_DAMAGE_MODIFIER = 2;
    private static final float AVERAGE_DAYS_PER_GROWTH = 5.0F;
    private static final float GROWTH_PROBABILITY_PER_RANDOM_TICK = 0.011377778F;
    private static final int MAX_GROWTH_LENGTH = 7;
    private static final int MAX_STALAGMITE_SEARCH_RANGE_WHEN_GROWING = 10;
    private static final float STALACTITE_DRIP_START_PIXEL = 0.6875F;
    private static final VoxelShape TIP_MERGE_SHAPE = Block.box(5.0, 0.0, 5.0, 11.0, 16.0, 11.0);
    private static final VoxelShape TIP_SHAPE_UP = Block.box(5.0, 0.0, 5.0, 11.0, 11.0, 11.0);
    private static final VoxelShape TIP_SHAPE_DOWN = Block.box(5.0, 5.0, 5.0, 11.0, 16.0, 11.0);
    private static final VoxelShape FRUSTUM_SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);
    private static final VoxelShape MIDDLE_SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0);
    private static final VoxelShape BASE_SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);
    private static final float MAX_HORIZONTAL_OFFSET = 0.125F;
    private static final VoxelShape REQUIRED_SPACE_TO_DRIP_THROUGH_NON_SOLID_BLOCK = Block.box(6.0, 0.0, 6.0, 10.0, 16.0, 10.0);

    public PointedDripstoneBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(
            this.stateDefinition
                .any()
                .setValue(TIP_DIRECTION, Direction.UP)
                .setValue(THICKNESS, DripstoneThickness.TIP)
                .setValue(WATERLOGGED, Boolean.valueOf(false))
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(TIP_DIRECTION, THICKNESS, WATERLOGGED);
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        return isValidPointedDripstonePlacement(param1, param2, param0.getValue(TIP_DIRECTION));
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param0.getValue(WATERLOGGED)) {
            param3.scheduleTick(param4, Fluids.WATER, Fluids.WATER.getTickDelay(param3));
        }

        if (param1 != Direction.UP && param1 != Direction.DOWN) {
            return param0;
        } else {
            Direction var0 = param0.getValue(TIP_DIRECTION);
            if (var0 == Direction.DOWN && param3.getBlockTicks().hasScheduledTick(param4, this)) {
                return param0;
            } else if (param1 == var0.getOpposite() && !this.canSurvive(param0, param3, param4)) {
                if (var0 == Direction.DOWN) {
                    param3.scheduleTick(param4, this, 2);
                } else {
                    param3.scheduleTick(param4, this, 1);
                }

                return param0;
            } else {
                boolean var1 = param0.getValue(THICKNESS) == DripstoneThickness.TIP_MERGE;
                DripstoneThickness var2 = calculateDripstoneThickness(param3, param4, var0, var1);
                return param0.setValue(THICKNESS, var2);
            }
        }
    }

    @Override
    public void onProjectileHit(Level param0, BlockState param1, BlockHitResult param2, Projectile param3) {
        BlockPos var0 = param2.getBlockPos();
        if (!param0.isClientSide && param3.mayInteract(param0, var0) && param3 instanceof ThrownTrident && param3.getDeltaMovement().length() > 0.6) {
            param0.destroyBlock(var0, true);
        }

    }

    @Override
    public void fallOn(Level param0, BlockState param1, BlockPos param2, Entity param3, float param4) {
        if (param1.getValue(TIP_DIRECTION) == Direction.UP && param1.getValue(THICKNESS) == DripstoneThickness.TIP) {
            param3.causeFallDamage(param4 + 2.0F, 2.0F, DamageSource.STALAGMITE);
        } else {
            super.fallOn(param0, param1, param2, param3, param4);
        }

    }

    @Override
    public void animateTick(BlockState param0, Level param1, BlockPos param2, RandomSource param3) {
        if (canDrip(param0)) {
            float var0 = param3.nextFloat();
            if (!(var0 > 0.12F)) {
                getFluidAboveStalactite(param1, param2, param0)
                    .filter(param1x -> var0 < 0.02F || canFillCauldron(param1x.fluid))
                    .ifPresent(param3x -> spawnDripParticle(param1, param2, param0, param3x.fluid));
            }
        }
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, RandomSource param3) {
        if (isStalagmite(param0) && !this.canSurvive(param0, param1, param2)) {
            param1.destroyBlock(param2, true);
        } else {
            spawnFallingStalactite(param0, param1, param2);
        }

    }

    @Override
    public void randomTick(BlockState param0, ServerLevel param1, BlockPos param2, RandomSource param3) {
        maybeTransferFluid(param0, param1, param2, param3.nextFloat());
        if (param3.nextFloat() < 0.011377778F && isStalactiteStartPos(param0, param1, param2)) {
            growStalactiteOrStalagmiteIfPossible(param0, param1, param2, param3);
        }

    }

    @VisibleForTesting
    public static void maybeTransferFluid(BlockState param0, ServerLevel param1, BlockPos param2, float param3) {
        if (!(param3 > 0.17578125F) || !(param3 > 0.05859375F)) {
            if (isStalactiteStartPos(param0, param1, param2)) {
                Optional<PointedDripstoneBlock.FluidInfo> var0 = getFluidAboveStalactite(param1, param2, param0);
                if (!var0.isEmpty()) {
                    Fluid var1 = var0.get().fluid;
                    float var2;
                    if (var1 == Fluids.WATER) {
                        var2 = 0.17578125F;
                    } else {
                        if (var1 != Fluids.LAVA) {
                            return;
                        }

                        var2 = 0.05859375F;
                    }

                    if (!(param3 >= var2)) {
                        BlockPos var5 = findTip(param0, param1, param2, 11, false);
                        if (var5 != null) {
                            if (var0.get().sourceState.is(Blocks.MUD) && var1 == Fluids.WATER) {
                                BlockState var6 = Blocks.CLAY.defaultBlockState();
                                param1.setBlockAndUpdate(var0.get().pos, var6);
                                param1.gameEvent(GameEvent.BLOCK_CHANGE, var0.get().pos, GameEvent.Context.of(var6));
                                param1.levelEvent(1504, var5, 0);
                            } else {
                                BlockPos var7 = findFillableCauldronBelowStalactiteTip(param1, var5, var1);
                                if (var7 != null) {
                                    param1.levelEvent(1504, var5, 0);
                                    int var8 = var5.getY() - var7.getY();
                                    int var9 = 50 + var8;
                                    BlockState var10 = param1.getBlockState(var7);
                                    param1.scheduleTick(var7, var10.getBlock(), var9);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState param0) {
        return PushReaction.DESTROY;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        LevelAccessor var0 = param0.getLevel();
        BlockPos var1 = param0.getClickedPos();
        Direction var2 = param0.getNearestLookingVerticalDirection().getOpposite();
        Direction var3 = calculateTipDirection(var0, var1, var2);
        if (var3 == null) {
            return null;
        } else {
            boolean var4 = !param0.isSecondaryUseActive();
            DripstoneThickness var5 = calculateDripstoneThickness(var0, var1, var3, var4);
            return var5 == null
                ? null
                : this.defaultBlockState()
                    .setValue(TIP_DIRECTION, var3)
                    .setValue(THICKNESS, var5)
                    .setValue(WATERLOGGED, Boolean.valueOf(var0.getFluidState(var1).getType() == Fluids.WATER));
        }
    }

    @Override
    public FluidState getFluidState(BlockState param0) {
        return param0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(param0);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState param0, BlockGetter param1, BlockPos param2) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        DripstoneThickness var0 = param0.getValue(THICKNESS);
        VoxelShape var1;
        if (var0 == DripstoneThickness.TIP_MERGE) {
            var1 = TIP_MERGE_SHAPE;
        } else if (var0 == DripstoneThickness.TIP) {
            if (param0.getValue(TIP_DIRECTION) == Direction.DOWN) {
                var1 = TIP_SHAPE_DOWN;
            } else {
                var1 = TIP_SHAPE_UP;
            }
        } else if (var0 == DripstoneThickness.FRUSTUM) {
            var1 = FRUSTUM_SHAPE;
        } else if (var0 == DripstoneThickness.MIDDLE) {
            var1 = MIDDLE_SHAPE;
        } else {
            var1 = BASE_SHAPE;
        }

        Vec3 var7 = param0.getOffset(param1, param2);
        return var1.move(var7.x, 0.0, var7.z);
    }

    @Override
    public boolean isCollisionShapeFullBlock(BlockState param0, BlockGetter param1, BlockPos param2) {
        return false;
    }

    @Override
    public float getMaxHorizontalOffset() {
        return 0.125F;
    }

    @Override
    public void onBrokenAfterFall(Level param0, BlockPos param1, FallingBlockEntity param2) {
        if (!param2.isSilent()) {
            param0.levelEvent(1045, param1, 0);
        }

    }

    @Override
    public DamageSource getFallDamageSource() {
        return DamageSource.FALLING_STALACTITE;
    }

    @Override
    public Predicate<Entity> getHurtsEntitySelector() {
        return EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(EntitySelector.LIVING_ENTITY_STILL_ALIVE);
    }

    private static void spawnFallingStalactite(BlockState param0, ServerLevel param1, BlockPos param2) {
        BlockPos.MutableBlockPos var0 = param2.mutable();

        for(BlockState var1 = param0; isStalactite(var1); var1 = param1.getBlockState(var0)) {
            FallingBlockEntity var2 = FallingBlockEntity.fall(param1, var0, var1);
            if (isTip(var1, true)) {
                int var3 = Math.max(1 + param2.getY() - var0.getY(), 6);
                float var4 = 1.0F * (float)var3;
                var2.setHurtsEntities(var4, 40);
                break;
            }

            var0.move(Direction.DOWN);
        }

    }

    @VisibleForTesting
    public static void growStalactiteOrStalagmiteIfPossible(BlockState param0, ServerLevel param1, BlockPos param2, RandomSource param3) {
        BlockState var0 = param1.getBlockState(param2.above(1));
        BlockState var1 = param1.getBlockState(param2.above(2));
        if (canGrow(var0, var1)) {
            BlockPos var2 = findTip(param0, param1, param2, 7, false);
            if (var2 != null) {
                BlockState var3 = param1.getBlockState(var2);
                if (canDrip(var3) && canTipGrow(var3, param1, var2)) {
                    if (param3.nextBoolean()) {
                        grow(param1, var2, Direction.DOWN);
                    } else {
                        growStalagmiteBelow(param1, var2);
                    }

                }
            }
        }
    }

    private static void growStalagmiteBelow(ServerLevel param0, BlockPos param1) {
        BlockPos.MutableBlockPos var0 = param1.mutable();

        for(int var1 = 0; var1 < 10; ++var1) {
            var0.move(Direction.DOWN);
            BlockState var2 = param0.getBlockState(var0);
            if (!var2.getFluidState().isEmpty()) {
                return;
            }

            if (isUnmergedTipWithDirection(var2, Direction.UP) && canTipGrow(var2, param0, var0)) {
                grow(param0, var0, Direction.UP);
                return;
            }

            if (isValidPointedDripstonePlacement(param0, var0, Direction.UP) && !param0.isWaterAt(var0.below())) {
                grow(param0, var0.below(), Direction.UP);
                return;
            }

            if (!canDripThrough(param0, var0, var2)) {
                return;
            }
        }

    }

    private static void grow(ServerLevel param0, BlockPos param1, Direction param2) {
        BlockPos var0 = param1.relative(param2);
        BlockState var1 = param0.getBlockState(var0);
        if (isUnmergedTipWithDirection(var1, param2.getOpposite())) {
            createMergedTips(var1, param0, var0);
        } else if (var1.isAir() || var1.is(Blocks.WATER)) {
            createDripstone(param0, var0, param2, DripstoneThickness.TIP);
        }

    }

    private static void createDripstone(LevelAccessor param0, BlockPos param1, Direction param2, DripstoneThickness param3) {
        BlockState var0 = Blocks.POINTED_DRIPSTONE
            .defaultBlockState()
            .setValue(TIP_DIRECTION, param2)
            .setValue(THICKNESS, param3)
            .setValue(WATERLOGGED, Boolean.valueOf(param0.getFluidState(param1).getType() == Fluids.WATER));
        param0.setBlock(param1, var0, 3);
    }

    private static void createMergedTips(BlockState param0, LevelAccessor param1, BlockPos param2) {
        BlockPos var1;
        BlockPos var0;
        if (param0.getValue(TIP_DIRECTION) == Direction.UP) {
            var0 = param2;
            var1 = param2.above();
        } else {
            var1 = param2;
            var0 = param2.below();
        }

        createDripstone(param1, var1, Direction.DOWN, DripstoneThickness.TIP_MERGE);
        createDripstone(param1, var0, Direction.UP, DripstoneThickness.TIP_MERGE);
    }

    public static void spawnDripParticle(Level param0, BlockPos param1, BlockState param2) {
        getFluidAboveStalactite(param0, param1, param2).ifPresent(param3 -> spawnDripParticle(param0, param1, param2, param3.fluid));
    }

    private static void spawnDripParticle(Level param0, BlockPos param1, BlockState param2, Fluid param3) {
        Vec3 var0 = param2.getOffset(param0, param1);
        double var1 = 0.0625;
        double var2 = (double)param1.getX() + 0.5 + var0.x;
        double var3 = (double)((float)(param1.getY() + 1) - 0.6875F) - 0.0625;
        double var4 = (double)param1.getZ() + 0.5 + var0.z;
        Fluid var5 = getDripFluid(param0, param3);
        ParticleOptions var6 = var5.is(FluidTags.LAVA) ? ParticleTypes.DRIPPING_DRIPSTONE_LAVA : ParticleTypes.DRIPPING_DRIPSTONE_WATER;
        param0.addParticle(var6, var2, var3, var4, 0.0, 0.0, 0.0);
    }

    @Nullable
    private static BlockPos findTip(BlockState param0, LevelAccessor param1, BlockPos param2, int param3, boolean param4) {
        if (isTip(param0, param4)) {
            return param2;
        } else {
            Direction var0 = param0.getValue(TIP_DIRECTION);
            BiPredicate<BlockPos, BlockState> var1 = (param1x, param2x) -> param2x.is(Blocks.POINTED_DRIPSTONE) && param2x.getValue(TIP_DIRECTION) == var0;
            return findBlockVertical(param1, param2, var0.getAxisDirection(), var1, param1x -> isTip(param1x, param4), param3).orElse(null);
        }
    }

    @Nullable
    private static Direction calculateTipDirection(LevelReader param0, BlockPos param1, Direction param2) {
        Direction var0;
        if (isValidPointedDripstonePlacement(param0, param1, param2)) {
            var0 = param2;
        } else {
            if (!isValidPointedDripstonePlacement(param0, param1, param2.getOpposite())) {
                return null;
            }

            var0 = param2.getOpposite();
        }

        return var0;
    }

    private static DripstoneThickness calculateDripstoneThickness(LevelReader param0, BlockPos param1, Direction param2, boolean param3) {
        Direction var0 = param2.getOpposite();
        BlockState var1 = param0.getBlockState(param1.relative(param2));
        if (isPointedDripstoneWithDirection(var1, var0)) {
            return !param3 && var1.getValue(THICKNESS) != DripstoneThickness.TIP_MERGE ? DripstoneThickness.TIP : DripstoneThickness.TIP_MERGE;
        } else if (!isPointedDripstoneWithDirection(var1, param2)) {
            return DripstoneThickness.TIP;
        } else {
            DripstoneThickness var2 = var1.getValue(THICKNESS);
            if (var2 != DripstoneThickness.TIP && var2 != DripstoneThickness.TIP_MERGE) {
                BlockState var3 = param0.getBlockState(param1.relative(var0));
                return !isPointedDripstoneWithDirection(var3, param2) ? DripstoneThickness.BASE : DripstoneThickness.MIDDLE;
            } else {
                return DripstoneThickness.FRUSTUM;
            }
        }
    }

    public static boolean canDrip(BlockState param0) {
        return isStalactite(param0) && param0.getValue(THICKNESS) == DripstoneThickness.TIP && !param0.getValue(WATERLOGGED);
    }

    private static boolean canTipGrow(BlockState param0, ServerLevel param1, BlockPos param2) {
        Direction var0 = param0.getValue(TIP_DIRECTION);
        BlockPos var1 = param2.relative(var0);
        BlockState var2 = param1.getBlockState(var1);
        if (!var2.getFluidState().isEmpty()) {
            return false;
        } else {
            return var2.isAir() ? true : isUnmergedTipWithDirection(var2, var0.getOpposite());
        }
    }

    private static Optional<BlockPos> findRootBlock(Level param0, BlockPos param1, BlockState param2, int param3) {
        Direction var0 = param2.getValue(TIP_DIRECTION);
        BiPredicate<BlockPos, BlockState> var1 = (param1x, param2x) -> param2x.is(Blocks.POINTED_DRIPSTONE) && param2x.getValue(TIP_DIRECTION) == var0;
        return findBlockVertical(param0, param1, var0.getOpposite().getAxisDirection(), var1, param0x -> !param0x.is(Blocks.POINTED_DRIPSTONE), param3);
    }

    private static boolean isValidPointedDripstonePlacement(LevelReader param0, BlockPos param1, Direction param2) {
        BlockPos var0 = param1.relative(param2.getOpposite());
        BlockState var1 = param0.getBlockState(var0);
        return var1.isFaceSturdy(param0, var0, param2) || isPointedDripstoneWithDirection(var1, param2);
    }

    private static boolean isTip(BlockState param0, boolean param1) {
        if (!param0.is(Blocks.POINTED_DRIPSTONE)) {
            return false;
        } else {
            DripstoneThickness var0 = param0.getValue(THICKNESS);
            return var0 == DripstoneThickness.TIP || param1 && var0 == DripstoneThickness.TIP_MERGE;
        }
    }

    private static boolean isUnmergedTipWithDirection(BlockState param0, Direction param1) {
        return isTip(param0, false) && param0.getValue(TIP_DIRECTION) == param1;
    }

    private static boolean isStalactite(BlockState param0) {
        return isPointedDripstoneWithDirection(param0, Direction.DOWN);
    }

    private static boolean isStalagmite(BlockState param0) {
        return isPointedDripstoneWithDirection(param0, Direction.UP);
    }

    private static boolean isStalactiteStartPos(BlockState param0, LevelReader param1, BlockPos param2) {
        return isStalactite(param0) && !param1.getBlockState(param2.above()).is(Blocks.POINTED_DRIPSTONE);
    }

    @Override
    public boolean isPathfindable(BlockState param0, BlockGetter param1, BlockPos param2, PathComputationType param3) {
        return false;
    }

    private static boolean isPointedDripstoneWithDirection(BlockState param0, Direction param1) {
        return param0.is(Blocks.POINTED_DRIPSTONE) && param0.getValue(TIP_DIRECTION) == param1;
    }

    @Nullable
    private static BlockPos findFillableCauldronBelowStalactiteTip(Level param0, BlockPos param1, Fluid param2) {
        Predicate<BlockState> var0 = param1x -> param1x.getBlock() instanceof AbstractCauldronBlock
                && ((AbstractCauldronBlock)param1x.getBlock()).canReceiveStalactiteDrip(param2);
        BiPredicate<BlockPos, BlockState> var1 = (param1x, param2x) -> canDripThrough(param0, param1x, param2x);
        return findBlockVertical(param0, param1, Direction.DOWN.getAxisDirection(), var1, var0, 11).orElse(null);
    }

    @Nullable
    public static BlockPos findStalactiteTipAboveCauldron(Level param0, BlockPos param1) {
        BiPredicate<BlockPos, BlockState> var0 = (param1x, param2) -> canDripThrough(param0, param1x, param2);
        return findBlockVertical(param0, param1, Direction.UP.getAxisDirection(), var0, PointedDripstoneBlock::canDrip, 11).orElse(null);
    }

    public static Fluid getCauldronFillFluidType(ServerLevel param0, BlockPos param1) {
        return getFluidAboveStalactite(param0, param1, param0.getBlockState(param1))
            .map(param0x -> param0x.fluid)
            .filter(PointedDripstoneBlock::canFillCauldron)
            .orElse(Fluids.EMPTY);
    }

    private static Optional<PointedDripstoneBlock.FluidInfo> getFluidAboveStalactite(Level param0, BlockPos param1, BlockState param2) {
        return !isStalactite(param2) ? Optional.empty() : findRootBlock(param0, param1, param2, 11).map(param1x -> {
            BlockPos var0x = param1x.above();
            BlockState var1x = param0.getBlockState(var0x);
            Fluid var2;
            if (var1x.is(Blocks.MUD) && !param0.dimensionType().ultraWarm()) {
                var2 = Fluids.WATER;
            } else {
                var2 = param0.getFluidState(var0x).getType();
            }

            return new PointedDripstoneBlock.FluidInfo(var0x, var2, var1x);
        });
    }

    private static boolean canFillCauldron(Fluid param0x) {
        return param0x == Fluids.LAVA || param0x == Fluids.WATER;
    }

    private static boolean canGrow(BlockState param0, BlockState param1) {
        return param0.is(Blocks.DRIPSTONE_BLOCK) && param1.is(Blocks.WATER) && param1.getFluidState().isSource();
    }

    private static Fluid getDripFluid(Level param0, Fluid param1) {
        if (param1.isSame(Fluids.EMPTY)) {
            return param0.dimensionType().ultraWarm() ? Fluids.LAVA : Fluids.WATER;
        } else {
            return param1;
        }
    }

    private static Optional<BlockPos> findBlockVertical(
        LevelAccessor param0,
        BlockPos param1,
        Direction.AxisDirection param2,
        BiPredicate<BlockPos, BlockState> param3,
        Predicate<BlockState> param4,
        int param5
    ) {
        Direction var0 = Direction.get(param2, Direction.Axis.Y);
        BlockPos.MutableBlockPos var1 = param1.mutable();

        for(int var2 = 1; var2 < param5; ++var2) {
            var1.move(var0);
            BlockState var3 = param0.getBlockState(var1);
            if (param4.test(var3)) {
                return Optional.of(var1.immutable());
            }

            if (param0.isOutsideBuildHeight(var1.getY()) || !param3.test(var1, var3)) {
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    private static boolean canDripThrough(BlockGetter param0, BlockPos param1, BlockState param2) {
        if (param2.isAir()) {
            return true;
        } else if (param2.isSolidRender(param0, param1)) {
            return false;
        } else if (!param2.getFluidState().isEmpty()) {
            return false;
        } else {
            VoxelShape var0 = param2.getCollisionShape(param0, param1);
            return !Shapes.joinIsNotEmpty(REQUIRED_SPACE_TO_DRIP_THROUGH_NON_SOLID_BLOCK, var0, BooleanOp.AND);
        }
    }

    static record FluidInfo(BlockPos pos, Fluid fluid, BlockState sourceState) {
    }
}

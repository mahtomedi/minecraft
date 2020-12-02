package net.minecraft.world.level.block;

import com.google.common.annotations.VisibleForTesting;
import java.util.Random;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
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
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PointedDripstoneBlock extends Block implements Fallable, SimpleWaterloggedBlock {
    public static final DirectionProperty TIP_DIRECTION = BlockStateProperties.VERTICAL_DIRECTION;
    public static final EnumProperty<DripstoneThickness> THICKNESS = BlockStateProperties.DRIPSTONE_THICKNESS;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final VoxelShape TIP_MERGE_SHAPE = Block.box(5.0, 0.0, 5.0, 11.0, 16.0, 11.0);
    private static final VoxelShape TIP_SHAPE_UP = Block.box(5.0, 0.0, 5.0, 11.0, 11.0, 11.0);
    private static final VoxelShape TIP_SHAPE_DOWN = Block.box(5.0, 5.0, 5.0, 11.0, 16.0, 11.0);
    private static final VoxelShape FRUSTUM_SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);
    private static final VoxelShape MIDDLE_SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0);
    private static final VoxelShape BASE_SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

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
            param3.getLiquidTicks().scheduleTick(param4, Fluids.WATER, Fluids.WATER.getTickDelay(param3));
        }

        if (param1 != Direction.UP && param1 != Direction.DOWN) {
            return param0;
        } else if (param3.getBlockTicks().hasScheduledTick(param4, this)) {
            return param0;
        } else {
            Direction var0 = param0.getValue(TIP_DIRECTION);
            if (param1 != var0.getOpposite() || isValidPointedDripstonePlacement(param3, param4, var0)) {
                boolean var1 = param0.getValue(THICKNESS) == DripstoneThickness.TIP_MERGE;
                DripstoneThickness var2 = calculateDripstoneThickness(param3, param4, var0, var1);
                return var2 == null ? this.getAirOrWater(param0) : param0.setValue(THICKNESS, var2);
            } else if (var0 == Direction.DOWN) {
                this.scheduleStalactiteFallTicks(param0, param3, param4);
                return param0;
            } else {
                return this.getAirOrWater(param0);
            }
        }
    }

    private BlockState getAirOrWater(BlockState param0) {
        return param0.getValue(WATERLOGGED) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
    }

    @Override
    public void onProjectileHit(Level param0, BlockState param1, BlockHitResult param2, Projectile param3) {
        if (param3 instanceof ThrownTrident && param3.getDeltaMovement().length() > 0.6) {
            param0.destroyBlock(param2.getBlockPos(), true);
        }

    }

    @Override
    public void fallOn(Level param0, BlockPos param1, Entity param2, float param3) {
        BlockState var0 = param0.getBlockState(param1);
        if (var0.getValue(TIP_DIRECTION) == Direction.UP && var0.getValue(THICKNESS) == DripstoneThickness.TIP) {
            param2.causeFallDamage(param3 + 2.0F, 2.0F);
        } else {
            super.fallOn(param0, param1, param2, param3);
        }

    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(BlockState param0, Level param1, BlockPos param2, Random param3) {
        if (canDrip(param0)) {
            float var0 = param3.nextFloat();
            if (!(var0 > 0.12F)) {
                PointedDripstoneBlock.DripType var1 = getDripType(param1, param2, param0);
                if (var1 != null) {
                    if (var0 < 0.02F || var1.canFillCauldron()) {
                        spawnDripParticle(param1, param2, param0, var1);
                    }

                }
            }
        }
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        spawnFallingStalactite(param0, param1, param2);
    }

    @Override
    public void randomTick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        maybeFillCauldron(param0, param1, param2, param3.nextFloat());
    }

    @VisibleForTesting
    public static void maybeFillCauldron(BlockState param0, ServerLevel param1, BlockPos param2, float param3) {
        if (!(param3 > 0.17578125F) || !(param3 > 0.05859375F)) {
            if (isStalactiteStartPos(param0, param1, param2)) {
                Fluid var0 = getCauldronFillFluidType(param1, param2);
                if (var0 != Fluids.EMPTY) {
                    BlockPos var1 = findTip(param0, param1, param2, 10);
                    if (var1 != null) {
                        BlockPos var2 = null;
                        if (var0 == Fluids.WATER && param3 < 0.17578125F || var0 == Fluids.LAVA && param3 < 0.05859375F) {
                            var2 = findFillableCauldronBelowStalactiteTip(param1, var1, var0);
                        }

                        if (var2 != null) {
                            param1.levelEvent(1504, var1, 0);
                            int var3 = var1.getY() - var2.getY();
                            int var4 = 50 + var3;
                            BlockState var5 = param1.getBlockState(var2);
                            param1.getBlockTicks().scheduleTick(var2, var5.getBlock(), var4);
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
    public BlockBehaviour.OffsetType getOffsetType() {
        return BlockBehaviour.OffsetType.XZ;
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

    private void scheduleStalactiteFallTicks(BlockState param0, LevelAccessor param1, BlockPos param2) {
        BlockPos var0 = findTip(param0, param1, param2, Integer.MAX_VALUE);
        if (var0 != null) {
            BlockPos.MutableBlockPos var1 = var0.mutable();

            while(isStalactite(param1.getBlockState(var1))) {
                param1.getBlockTicks().scheduleTick(var1, this, 2);
                var1.move(Direction.UP);
            }

        }
    }

    private static int getStalactiteSizeFromTip(ServerLevel param0, BlockPos param1, int param2) {
        int var0 = 1;
        BlockPos.MutableBlockPos var1 = param1.mutable().move(Direction.UP);

        while(var0 < param2 && isStalactite(param0.getBlockState(var1))) {
            ++var0;
            var1.move(Direction.UP);
        }

        return var0;
    }

    private static void spawnFallingStalactite(BlockState param0, ServerLevel param1, BlockPos param2) {
        Vec3 var0 = Vec3.atBottomCenterOf(param2);
        FallingBlockEntity var1 = new FallingBlockEntity(param1, var0.x, var0.y, var0.z, param0);
        if (isTip(param0)) {
            int var2 = getStalactiteSizeFromTip(param1, param2, 6);
            float var3 = 1.0F * (float)var2;
            var1.setHurtsEntities(var3, 40);
        }

        param1.addFreshEntity(var1);
    }

    @OnlyIn(Dist.CLIENT)
    public static void spawnDripParticle(Level param0, BlockPos param1, BlockState param2) {
        PointedDripstoneBlock.DripType var0 = getDripType(param0, param1, param2);
        if (var0 != null) {
            spawnDripParticle(param0, param1, param2, var0);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void spawnDripParticle(Level param0, BlockPos param1, BlockState param2, PointedDripstoneBlock.DripType param3) {
        Vec3 var0 = param2.getOffset(param0, param1);
        double var1 = 0.0625;
        double var2 = (double)param1.getX() + 0.5 + var0.x;
        double var3 = (double)((float)(param1.getY() + 1) - 0.6875F) - 0.0625;
        double var4 = (double)param1.getZ() + 0.5 + var0.z;
        param0.addParticle(param3.getDripParticle(), var2, var3, var4, 0.0, 0.0, 0.0);
    }

    @Nullable
    private static BlockPos findTip(BlockState param0, LevelAccessor param1, BlockPos param2, int param3) {
        Direction var0 = param0.getValue(TIP_DIRECTION);
        BlockPos.MutableBlockPos var1 = param2.mutable();
        BlockState var2 = param0;
        int var3 = param1.getMinBuildHeight();

        for(int var4 = 0; var4 < param3 && var1.getY() > var3; ++var4) {
            if (isTip(var2)) {
                return var1;
            }

            if (!var2.is(Blocks.POINTED_DRIPSTONE) || var2.getValue(TIP_DIRECTION) != var0) {
                return null;
            }

            var1.move(var0);
            var2 = param1.getBlockState(var1);
        }

        return null;
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

    @Nullable
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
                if (var2 != DripstoneThickness.FRUSTUM && var2 != DripstoneThickness.MIDDLE) {
                    return null;
                } else {
                    return isPointedDripstoneWithDirection(var3, param2) ? DripstoneThickness.MIDDLE : DripstoneThickness.BASE;
                }
            } else {
                return DripstoneThickness.FRUSTUM;
            }
        }
    }

    public static boolean canDrip(BlockState param0) {
        return isStalactite(param0) && param0.getValue(THICKNESS) == DripstoneThickness.TIP && !param0.getValue(WATERLOGGED);
    }

    @Nullable
    private static BlockPos findRootBlock(Level param0, BlockPos param1, BlockState param2, int param3) {
        Direction var0 = param2.getValue(TIP_DIRECTION).getOpposite();
        BlockPos.MutableBlockPos var1 = param1.mutable();

        for(int var2 = 0; var2 < param3; ++var2) {
            var1.move(var0);
            BlockState var3 = param0.getBlockState(var1);
            if (!var3.is(Blocks.POINTED_DRIPSTONE)) {
                return var1;
            }
        }

        return null;
    }

    private static boolean isValidPointedDripstonePlacement(LevelReader param0, BlockPos param1, Direction param2) {
        BlockPos var0 = param1.relative(param2.getOpposite());
        BlockState var1 = param0.getBlockState(var0);
        return var1.isFaceSturdy(param0, var0, param2) || isPointedDripstoneWithDirection(var1, param2);
    }

    private static boolean isTip(BlockState param0) {
        if (!param0.is(Blocks.POINTED_DRIPSTONE)) {
            return false;
        } else {
            DripstoneThickness var0 = param0.getValue(THICKNESS);
            return var0 == DripstoneThickness.TIP || var0 == DripstoneThickness.TIP_MERGE;
        }
    }

    private static boolean isStalactite(BlockState param0) {
        return isPointedDripstoneWithDirection(param0, Direction.DOWN);
    }

    private static boolean isStalactiteStartPos(BlockState param0, LevelReader param1, BlockPos param2) {
        return isStalactite(param0) && !param1.getBlockState(param2.above()).is(Blocks.POINTED_DRIPSTONE);
    }

    private static boolean isPointedDripstoneWithDirection(BlockState param0, Direction param1) {
        return param0.is(Blocks.POINTED_DRIPSTONE) && param0.getValue(TIP_DIRECTION) == param1;
    }

    @Nullable
    private static BlockPos findFillableCauldronBelowStalactiteTip(Level param0, BlockPos param1, Fluid param2) {
        BlockPos.MutableBlockPos var0 = param1.mutable();

        for(int var1 = 0; var1 < 10; ++var1) {
            var0.move(Direction.DOWN);
            BlockState var2 = param0.getBlockState(var0);
            if (!var2.isAir()) {
                if (var2.getBlock() instanceof AbstractCauldronBlock) {
                    AbstractCauldronBlock var3 = (AbstractCauldronBlock)var2.getBlock();
                    if (var3.canReceiveStalactiteDrip(param2)) {
                        return var0;
                    }
                }

                return null;
            }
        }

        return null;
    }

    @Nullable
    public static BlockPos findStalactiteTipAboveCauldron(Level param0, BlockPos param1) {
        BlockPos.MutableBlockPos var0 = param1.mutable();

        for(int var1 = 0; var1 < 10; ++var1) {
            var0.move(Direction.UP);
            BlockState var2 = param0.getBlockState(var0);
            if (!var2.isAir()) {
                if (canDrip(var2)) {
                    return var0;
                }

                return null;
            }
        }

        return null;
    }

    public static Fluid getCauldronFillFluidType(Level param0, BlockPos param1) {
        PointedDripstoneBlock.DripType var0 = getDripType(param0, param1, param0.getBlockState(param1));
        return var0 != null && var0.canFillCauldron() ? var0.getDripFluid() : Fluids.EMPTY;
    }

    @Nullable
    private static PointedDripstoneBlock.DripType getDripType(Level param0, BlockPos param1, BlockState param2) {
        if (!isStalactite(param2)) {
            return null;
        } else {
            BlockPos var0 = findRootBlock(param0, param1, param2, 10);
            if (var0 == null) {
                return null;
            } else {
                FluidState var1 = param0.getFluidState(var0.above());
                return new PointedDripstoneBlock.DripType(var1.getType(), param0.dimensionType());
            }
        }
    }

    static class DripType {
        private final Fluid fluidAboveRootBlock;
        private final DimensionType dimensionType;

        DripType(Fluid param0, DimensionType param1) {
            this.fluidAboveRootBlock = param0;
            this.dimensionType = param1;
        }

        Fluid getDripFluid() {
            return !this.fluidAboveRootBlock.is(FluidTags.LAVA) && !this.dimensionType.ultraWarm() ? Fluids.WATER : Fluids.LAVA;
        }

        boolean canFillCauldron() {
            return this.fluidAboveRootBlock == Fluids.LAVA || this.fluidAboveRootBlock == Fluids.WATER;
        }

        @OnlyIn(Dist.CLIENT)
        ParticleOptions getDripParticle() {
            return this.getDripFluid() == Fluids.WATER ? ParticleTypes.DRIPPING_DRIPSTONE_WATER : ParticleTypes.DRIPPING_DRIPSTONE_LAVA;
        }
    }
}

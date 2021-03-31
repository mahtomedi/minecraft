package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class LightningRodBlock extends RodBlock implements SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private static final int ACTIVATION_TICKS = 8;
    public static final int RANGE = 128;
    public static final int SPARK_CYCLE = 200;

    public LightningRodBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(
            this.stateDefinition.any().setValue(FACING, Direction.UP).setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(POWERED, Boolean.valueOf(false))
        );
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        FluidState var0 = param0.getLevel().getFluidState(param0.getClickedPos());
        boolean var1 = var0.getType() == Fluids.WATER;
        return this.defaultBlockState().setValue(FACING, param0.getClickedFace()).setValue(WATERLOGGED, Boolean.valueOf(var1));
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param0.getValue(WATERLOGGED)) {
            param3.getLiquidTicks().scheduleTick(param4, Fluids.WATER, Fluids.WATER.getTickDelay(param3));
        }

        return super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public FluidState getFluidState(BlockState param0) {
        return param0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(param0);
    }

    @Override
    public int getSignal(BlockState param0, BlockGetter param1, BlockPos param2, Direction param3) {
        return param0.getValue(POWERED) ? 15 : 0;
    }

    @Override
    public int getDirectSignal(BlockState param0, BlockGetter param1, BlockPos param2, Direction param3) {
        return param0.getValue(POWERED) && param0.getValue(FACING) == param3 ? 15 : 0;
    }

    public void onLightningStrike(BlockState param0, Level param1, BlockPos param2) {
        param1.setBlock(param2, param0.setValue(POWERED, Boolean.valueOf(true)), 3);
        this.updateNeighbours(param0, param1, param2);
        param1.getBlockTicks().scheduleTick(param2, this, 8);
        param1.levelEvent(3002, param2, param0.getValue(FACING).getAxis().ordinal());
    }

    private void updateNeighbours(BlockState param0, Level param1, BlockPos param2) {
        param1.updateNeighborsAt(param2.relative(param0.getValue(FACING).getOpposite()), this);
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        param1.setBlock(param2, param0.setValue(POWERED, Boolean.valueOf(false)), 3);
        this.updateNeighbours(param0, param1, param2);
    }

    @Override
    public void animateTick(BlockState param0, Level param1, BlockPos param2, Random param3) {
        if (param1.isThundering()
            && (long)param1.random.nextInt(200) <= param1.getGameTime() % 200L
            && param2.getY() == param1.getHeight(Heightmap.Types.WORLD_SURFACE, param2.getX(), param2.getZ()) - 1) {
            ParticleUtils.spawnParticlesAlongAxis(param0.getValue(FACING).getAxis(), param1, param2, 0.125, ParticleTypes.ELECTRIC_SPARK, UniformInt.of(1, 2));
        }
    }

    public static double getSparkPositionOnAxis(Random param0, Direction.Axis param1, double param2, Direction.Axis param3) {
        double var0 = param1 == param3 ? 1.0 : 0.25;
        return param2 + param0.nextDouble() * var0 - var0 / 2.0;
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
    public void onProjectileHit(Level param0, BlockState param1, BlockHitResult param2, Projectile param3) {
        if (param0.isThundering() && param3 instanceof ThrownTrident && ((ThrownTrident)param3).isChanneling()) {
            BlockPos var0 = param2.getBlockPos();
            if (param0.canSeeSky(var0)) {
                LightningBolt var1 = EntityType.LIGHTNING_BOLT.create(param0);
                var1.moveTo(Vec3.atBottomCenterOf(var0.above()));
                Entity var2 = param3.getOwner();
                var1.setCause(var2 instanceof ServerPlayer ? (ServerPlayer)var2 : null);
                param0.addFreshEntity(var1);
                param0.playSound(null, var0, SoundEvents.TRIDENT_THUNDER, SoundSource.WEATHER, 5.0F, 1.0F);
            }
        }

    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(FACING, POWERED, WATERLOGGED);
    }

    @Override
    public boolean isSignalSource(BlockState param0) {
        return true;
    }
}

package net.minecraft.world.level.block;

import java.util.Optional;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.DefaultExplosionDamageCalculator;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RespawnAnchorBlock extends Block {
    public static final IntegerProperty CHARGE = BlockStateProperties.RESPAWN_ANCHOR_CHARGES;

    public RespawnAnchorBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(CHARGE, Integer.valueOf(0)));
    }

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        ItemStack var0 = param3.getItemInHand(param4);
        if (param4 == InteractionHand.MAIN_HAND && !isRespawnFuel(var0) && isRespawnFuel(param3.getItemInHand(InteractionHand.OFF_HAND))) {
            return InteractionResult.PASS;
        } else if (isRespawnFuel(var0) && canBeCharged(param0)) {
            charge(param1, param2, param0);
            if (!param3.abilities.instabuild) {
                var0.shrink(1);
            }

            return InteractionResult.sidedSuccess(param1.isClientSide);
        } else if (param0.getValue(CHARGE) == 0) {
            return InteractionResult.PASS;
        } else if (!canSetSpawn(param1)) {
            if (!param1.isClientSide) {
                this.explode(param0, param1, param2);
            }

            return InteractionResult.sidedSuccess(param1.isClientSide);
        } else {
            if (!param1.isClientSide) {
                ServerPlayer var1 = (ServerPlayer)param3;
                if (var1.getRespawnDimension() != param1.dimension() || !var1.getRespawnPosition().equals(param2)) {
                    var1.setRespawnPosition(param1.dimension(), param2, false, true);
                    param1.playSound(
                        null,
                        (double)param2.getX() + 0.5,
                        (double)param2.getY() + 0.5,
                        (double)param2.getZ() + 0.5,
                        SoundEvents.RESPAWN_ANCHOR_SET_SPAWN,
                        SoundSource.BLOCKS,
                        1.0F,
                        1.0F
                    );
                    return InteractionResult.SUCCESS;
                }
            }

            return canBeCharged(param0) ? InteractionResult.PASS : InteractionResult.CONSUME;
        }
    }

    private static boolean isRespawnFuel(ItemStack param0) {
        return param0.getItem() == Items.GLOWSTONE;
    }

    private static boolean canBeCharged(BlockState param0) {
        return param0.getValue(CHARGE) < 4;
    }

    private static boolean isWaterThatWouldFlow(BlockPos param0, Level param1) {
        FluidState var0 = param1.getFluidState(param0);
        if (!var0.is(FluidTags.WATER)) {
            return false;
        } else if (var0.isSource()) {
            return true;
        } else {
            float var1 = (float)var0.getAmount();
            if (var1 < 2.0F) {
                return false;
            } else {
                FluidState var2 = param1.getFluidState(param0.below());
                return !var2.is(FluidTags.WATER);
            }
        }
    }

    private void explode(BlockState param0, Level param1, final BlockPos param2) {
        param1.removeBlock(param2, false);
        boolean var0 = Direction.Plane.HORIZONTAL.stream().map(param2::relative).anyMatch(param1x -> isWaterThatWouldFlow(param1x, param1));
        final boolean var1 = var0 || param1.getFluidState(param2.above()).is(FluidTags.WATER);
        ExplosionDamageCalculator var2 = new ExplosionDamageCalculator() {
            @Override
            public Optional<Float> getBlockExplosionResistance(Explosion param0, BlockGetter param1, BlockPos param2x, BlockState param3, FluidState param4) {
                return param2.equals(param2) && var1
                    ? Optional.of(Blocks.WATER.getExplosionResistance())
                    : DefaultExplosionDamageCalculator.INSTANCE.getBlockExplosionResistance(param0, param1, param2, param3, param4);
            }

            @Override
            public boolean shouldBlockExplode(Explosion param0, BlockGetter param1, BlockPos param2x, BlockState param3, float param4) {
                return DefaultExplosionDamageCalculator.INSTANCE.shouldBlockExplode(param0, param1, param2, param3, param4);
            }
        };
        param1.explode(
            null,
            DamageSource.badRespawnPointExplosion(),
            var2,
            (double)param2.getX() + 0.5,
            (double)param2.getY() + 0.5,
            (double)param2.getZ() + 0.5,
            5.0F,
            true,
            Explosion.BlockInteraction.DESTROY
        );
    }

    public static boolean canSetSpawn(Level param0) {
        return param0.dimensionType().respawnAnchorWorks();
    }

    public static void charge(Level param0, BlockPos param1, BlockState param2) {
        param0.setBlock(param1, param2.setValue(CHARGE, Integer.valueOf(param2.getValue(CHARGE) + 1)), 3);
        param0.playSound(
            null,
            (double)param1.getX() + 0.5,
            (double)param1.getY() + 0.5,
            (double)param1.getZ() + 0.5,
            SoundEvents.RESPAWN_ANCHOR_CHARGE,
            SoundSource.BLOCKS,
            1.0F,
            1.0F
        );
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(BlockState param0, Level param1, BlockPos param2, Random param3) {
        if (param0.getValue(CHARGE) != 0) {
            if (param3.nextInt(100) == 0) {
                param1.playSound(
                    null,
                    (double)param2.getX() + 0.5,
                    (double)param2.getY() + 0.5,
                    (double)param2.getZ() + 0.5,
                    SoundEvents.RESPAWN_ANCHOR_AMBIENT,
                    SoundSource.BLOCKS,
                    1.0F,
                    1.0F
                );
            }

            double var0 = (double)param2.getX() + 0.5 + (0.5 - param3.nextDouble());
            double var1 = (double)param2.getY() + 1.0;
            double var2 = (double)param2.getZ() + 0.5 + (0.5 - param3.nextDouble());
            double var3 = (double)param3.nextFloat() * 0.04;
            param1.addParticle(ParticleTypes.REVERSE_PORTAL, var0, var1, var2, 0.0, var3, 0.0);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(CHARGE);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState param0) {
        return true;
    }

    public static int getScaledChargeLevel(BlockState param0, int param1) {
        return Mth.floor((float)(param0.getValue(CHARGE) - 0) / 4.0F * (float)param1);
    }

    @Override
    public int getAnalogOutputSignal(BlockState param0, Level param1, BlockPos param2) {
        return getScaledChargeLevel(param0, 15);
    }

    public static Optional<Vec3> findStandUpPosition(EntityType<?> param0, LevelReader param1, BlockPos param2) {
        for(BlockPos var0 : BlockPos.betweenClosed(param2.offset(-1, -1, -1), param2.offset(1, 1, 1))) {
            Optional<Vec3> var1 = BedBlock.getStandingLocationAtOrBelow(param0, param1, var0);
            if (var1.isPresent()) {
                return var1;
            }
        }

        return Optional.empty();
    }

    @Override
    public boolean isPathfindable(BlockState param0, BlockGetter param1, BlockPos param2, PathComputationType param3) {
        return false;
    }
}

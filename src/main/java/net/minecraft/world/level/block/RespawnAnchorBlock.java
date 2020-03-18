package net.minecraft.world.level.block;

import java.util.Optional;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.dimension.DimensionType;
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
    public void onPlace(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        super.onPlace(param0, param1, param2, param3, param4);
        param1.getBlockTicks().scheduleTick(param2, param0.getBlock(), 20);
    }

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        int var0 = param0.getValue(CHARGE);
        if (param3.getItemInHand(param4).getItem() == Items.GLOWSTONE) {
            if (var0 < 4) {
                param1.setBlock(param2, param0.setValue(CHARGE, Integer.valueOf(var0 + 1)), 3);
                param1.playLocalSound(
                    (double)param2.getX() + 0.5,
                    (double)param2.getY() + 0.5,
                    (double)param2.getZ() + 0.5,
                    SoundEvents.RESPAWN_ANCHOR_CHARGE,
                    SoundSource.BLOCKS,
                    1.0F,
                    1.0F,
                    false
                );
                return InteractionResult.SUCCESS;
            } else {
                return InteractionResult.CONSUME;
            }
        } else if (var0 == 0) {
            return InteractionResult.CONSUME;
        } else {
            if (param1.dimension.getType() == DimensionType.NETHER) {
                if (!param1.isClientSide) {
                    ((ServerPlayer)param3).setRespawnPosition(param1.dimension.getType(), param2, false, true);
                }

                param1.playLocalSound(
                    (double)param2.getX() + 0.5,
                    (double)param2.getY() + 0.5,
                    (double)param2.getZ() + 0.5,
                    SoundEvents.RESPAWN_ANCHOR_SET_SPAWN,
                    SoundSource.BLOCKS,
                    1.0F,
                    1.0F,
                    false
                );
            } else {
                param1.removeBlock(param2, false);
                param1.explode(
                    null,
                    DamageSource.netherBedExplosion(),
                    (double)param2.getX() + 0.5,
                    (double)param2.getY() + 0.5,
                    (double)param2.getZ() + 0.5,
                    5.0F,
                    true,
                    Explosion.BlockInteraction.DESTROY
                );
            }

            return InteractionResult.SUCCESS;
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(BlockState param0, Level param1, BlockPos param2, Random param3) {
        if (param0.getValue(CHARGE) != 0) {
            if (param3.nextInt(100) == 0) {
                param1.playLocalSound(
                    (double)param2.getX() + 0.5,
                    (double)param2.getY() + 0.5,
                    (double)param2.getZ() + 0.5,
                    SoundEvents.RESPAWN_ANCHOR_AMBIENT,
                    SoundSource.BLOCKS,
                    0.5F,
                    param3.nextFloat() * 0.4F + 0.8F,
                    false
                );
            }

            double var0 = (double)param2.getX() + 0.5 + (double)(0.5F - param3.nextFloat());
            double var1 = (double)param2.getY() + 1.0;
            double var2 = (double)param2.getZ() + 0.5 + (double)(0.5F - param3.nextFloat());
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

    public static Optional<Vec3> findStandUpPosition(LevelReader param0, BlockPos param1) {
        for(BlockPos var0 : BlockPos.betweenClosed(param1.offset(-1, 0, -1), param1.offset(1, 0, 1))) {
            BlockPos var1 = var0.below();
            BlockPos var2 = var0.above();
            if (param0.getBlockState(var1).isFaceSturdy(param0, var1, Direction.DOWN)
                && param0.getBlockState(var0).getCollisionShape(param0, var0).isEmpty()
                && param0.getBlockState(var2).getCollisionShape(param0, var2).isEmpty()) {
                return Optional.of(Vec3.atBottomCenterOf(var0));
            }
        }

        return Optional.empty();
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState param0) {
        return true;
    }

    @Override
    public boolean isPathfindable(BlockState param0, BlockGetter param1, BlockPos param2, PathComputationType param3) {
        return false;
    }
}

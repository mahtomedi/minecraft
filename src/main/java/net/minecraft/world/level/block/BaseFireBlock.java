package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class BaseFireBlock extends Block {
    private final float fireDamage;
    protected static final VoxelShape UP_AABB = Block.box(0.0, 15.0, 0.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape DOWN_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
    protected static final VoxelShape WEST_AABB = Block.box(0.0, 0.0, 0.0, 1.0, 16.0, 16.0);
    protected static final VoxelShape EAST_AABB = Block.box(15.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape NORTH_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 1.0);
    protected static final VoxelShape SOUTH_AABB = Block.box(0.0, 0.0, 15.0, 16.0, 16.0, 16.0);

    public BaseFireBlock(BlockBehaviour.Properties param0, float param1) {
        super(param0);
        this.fireDamage = param1;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        return getState(param0.getLevel(), param0.getClickedPos());
    }

    public static BlockState getState(BlockGetter param0, BlockPos param1) {
        BlockPos var0 = param1.below();
        BlockState var1 = param0.getBlockState(var0);
        return SoulFireBlock.canSurviveOnBlock(var1.getBlock())
            ? Blocks.SOUL_FIRE.defaultBlockState()
            : ((FireBlock)Blocks.FIRE).getStateForPlacement(param0, param1);
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return DOWN_AABB;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(BlockState param0, Level param1, BlockPos param2, Random param3) {
        if (param3.nextInt(24) == 0) {
            param1.playLocalSound(
                (double)((float)param2.getX() + 0.5F),
                (double)((float)param2.getY() + 0.5F),
                (double)((float)param2.getZ() + 0.5F),
                SoundEvents.FIRE_AMBIENT,
                SoundSource.BLOCKS,
                1.0F + param3.nextFloat(),
                param3.nextFloat() * 0.7F + 0.3F,
                false
            );
        }

        BlockPos var0 = param2.below();
        BlockState var1 = param1.getBlockState(var0);
        if (!this.canBurn(var1) && !var1.isFaceSturdy(param1, var0, Direction.UP)) {
            if (this.canBurn(param1.getBlockState(param2.west()))) {
                for(int var6 = 0; var6 < 2; ++var6) {
                    double var7 = (double)param2.getX() + param3.nextDouble() * 0.1F;
                    double var8 = (double)param2.getY() + param3.nextDouble();
                    double var9 = (double)param2.getZ() + param3.nextDouble();
                    param1.addParticle(ParticleTypes.LARGE_SMOKE, var7, var8, var9, 0.0, 0.0, 0.0);
                }
            }

            if (this.canBurn(param1.getBlockState(param2.east()))) {
                for(int var10 = 0; var10 < 2; ++var10) {
                    double var11 = (double)(param2.getX() + 1) - param3.nextDouble() * 0.1F;
                    double var12 = (double)param2.getY() + param3.nextDouble();
                    double var13 = (double)param2.getZ() + param3.nextDouble();
                    param1.addParticle(ParticleTypes.LARGE_SMOKE, var11, var12, var13, 0.0, 0.0, 0.0);
                }
            }

            if (this.canBurn(param1.getBlockState(param2.north()))) {
                for(int var14 = 0; var14 < 2; ++var14) {
                    double var15 = (double)param2.getX() + param3.nextDouble();
                    double var16 = (double)param2.getY() + param3.nextDouble();
                    double var17 = (double)param2.getZ() + param3.nextDouble() * 0.1F;
                    param1.addParticle(ParticleTypes.LARGE_SMOKE, var15, var16, var17, 0.0, 0.0, 0.0);
                }
            }

            if (this.canBurn(param1.getBlockState(param2.south()))) {
                for(int var18 = 0; var18 < 2; ++var18) {
                    double var19 = (double)param2.getX() + param3.nextDouble();
                    double var20 = (double)param2.getY() + param3.nextDouble();
                    double var21 = (double)(param2.getZ() + 1) - param3.nextDouble() * 0.1F;
                    param1.addParticle(ParticleTypes.LARGE_SMOKE, var19, var20, var21, 0.0, 0.0, 0.0);
                }
            }

            if (this.canBurn(param1.getBlockState(param2.above()))) {
                for(int var22 = 0; var22 < 2; ++var22) {
                    double var23 = (double)param2.getX() + param3.nextDouble();
                    double var24 = (double)(param2.getY() + 1) - param3.nextDouble() * 0.1F;
                    double var25 = (double)param2.getZ() + param3.nextDouble();
                    param1.addParticle(ParticleTypes.LARGE_SMOKE, var23, var24, var25, 0.0, 0.0, 0.0);
                }
            }
        } else {
            for(int var2 = 0; var2 < 3; ++var2) {
                double var3 = (double)param2.getX() + param3.nextDouble();
                double var4 = (double)param2.getY() + param3.nextDouble() * 0.5 + 0.5;
                double var5 = (double)param2.getZ() + param3.nextDouble();
                param1.addParticle(ParticleTypes.LARGE_SMOKE, var3, var4, var5, 0.0, 0.0, 0.0);
            }
        }

    }

    protected abstract boolean canBurn(BlockState var1);

    @Override
    public void entityInside(BlockState param0, Level param1, BlockPos param2, Entity param3) {
        if (!param3.fireImmune() && (!(param3 instanceof LivingEntity) || !EnchantmentHelper.hasFrostWalker((LivingEntity)param3))) {
            param3.setRemainingFireTicks(param3.getRemainingFireTicks() + 1);
            if (param3.getRemainingFireTicks() == 0) {
                param3.setSecondsOnFire(8);
            }

            param3.hurt(DamageSource.IN_FIRE, this.fireDamage);
        }

        super.entityInside(param0, param1, param2, param3);
    }

    @Override
    public void onPlace(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        if (!param3.is(param0.getBlock())) {
            if (!param1.dimensionType().isOverworld() && !param1.dimensionType().isNether() || !NetherPortalBlock.trySpawnPortal(param1, param2)) {
                if (!param0.canSurvive(param1, param2)) {
                    param1.removeBlock(param2, false);
                }

            }
        }
    }

    @Override
    public void playerWillDestroy(Level param0, BlockPos param1, BlockState param2, Player param3) {
        if (!param0.isClientSide()) {
            param0.levelEvent(null, 1009, param1, 0);
        }

    }
}

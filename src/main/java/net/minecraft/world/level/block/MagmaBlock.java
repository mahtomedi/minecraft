package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockAndBiomeGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MagmaBlock extends Block {
    public MagmaBlock(Block.Properties param0) {
        super(param0);
    }

    @Override
    public void stepOn(Level param0, BlockPos param1, Entity param2) {
        if (!param2.fireImmune() && param2 instanceof LivingEntity && !EnchantmentHelper.hasFrostWalker((LivingEntity)param2)) {
            param2.hurt(DamageSource.HOT_FLOOR, 1.0F);
        }

        super.stepOn(param0, param1, param2);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getLightColor(BlockState param0, BlockAndBiomeGetter param1, BlockPos param2) {
        return 15728880;
    }

    @Override
    public void tick(BlockState param0, Level param1, BlockPos param2, Random param3) {
        BubbleColumnBlock.growColumn(param1, param2.above(), true);
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param1 == Direction.UP && param2.getBlock() == Blocks.WATER) {
            param3.getBlockTicks().scheduleTick(param4, this, this.getTickDelay(param3));
        }

        return super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public void randomTick(BlockState param0, Level param1, BlockPos param2, Random param3) {
        BlockPos var0 = param2.above();
        if (param1.getFluidState(param2).is(FluidTags.WATER)) {
            param1.playSound(
                null, param2, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (param1.random.nextFloat() - param1.random.nextFloat()) * 0.8F
            );
            if (param1 instanceof ServerLevel) {
                ((ServerLevel)param1)
                    .sendParticles(
                        ParticleTypes.LARGE_SMOKE, (double)var0.getX() + 0.5, (double)var0.getY() + 0.25, (double)var0.getZ() + 0.5, 8, 0.5, 0.25, 0.5, 0.0
                    );
            }
        }

    }

    @Override
    public int getTickDelay(LevelReader param0) {
        return 20;
    }

    @Override
    public void onPlace(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        param1.getBlockTicks().scheduleTick(param2, this, this.getTickDelay(param1));
    }

    @Override
    public boolean isValidSpawn(BlockState param0, BlockGetter param1, BlockPos param2, EntityType<?> param3) {
        return param3.fireImmune();
    }

    @Override
    public boolean hasPostProcess(BlockState param0, BlockGetter param1, BlockPos param2) {
        return true;
    }
}

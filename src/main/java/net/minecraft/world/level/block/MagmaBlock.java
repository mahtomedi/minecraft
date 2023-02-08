package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class MagmaBlock extends Block {
    private static final int BUBBLE_COLUMN_CHECK_DELAY = 20;

    public MagmaBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    public void stepOn(Level param0, BlockPos param1, BlockState param2, Entity param3) {
        if (!param3.isSteppingCarefully() && param3 instanceof LivingEntity && !EnchantmentHelper.hasFrostWalker((LivingEntity)param3)) {
            param3.hurt(param0.damageSources().hotFloor(), 1.0F);
        }

        super.stepOn(param0, param1, param2, param3);
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, RandomSource param3) {
        BubbleColumnBlock.updateColumn(param1, param2.above(), param0);
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param1 == Direction.UP && param2.is(Blocks.WATER)) {
            param3.scheduleTick(param4, this, 20);
        }

        return super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public void randomTick(BlockState param0, ServerLevel param1, BlockPos param2, RandomSource param3) {
        BlockPos var0 = param2.above();
        if (param1.getFluidState(param2).is(FluidTags.WATER)) {
            param1.playSound(
                null, param2, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (param1.random.nextFloat() - param1.random.nextFloat()) * 0.8F
            );
            param1.sendParticles(
                ParticleTypes.LARGE_SMOKE, (double)var0.getX() + 0.5, (double)var0.getY() + 0.25, (double)var0.getZ() + 0.5, 8, 0.5, 0.25, 0.5, 0.0
            );
        }

    }

    @Override
    public void onPlace(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        param1.scheduleTick(param2, this, 20);
    }
}

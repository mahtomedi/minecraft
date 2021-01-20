package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;

public class AmethystBlock extends Block {
    public AmethystBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState param0) {
        return PushReaction.NORMAL;
    }

    @Override
    public void onProjectileHit(Level param0, BlockState param1, BlockHitResult param2, Projectile param3) {
        if (!param0.isClientSide) {
            BlockPos var0 = param2.getBlockPos();
            param0.playSound(null, var0, SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.BLOCKS, 1.0F, 0.5F + param0.random.nextFloat() * 1.2F);
            param0.playSound(null, var0, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, 0.5F + param0.random.nextFloat() * 1.2F);
        }

    }
}

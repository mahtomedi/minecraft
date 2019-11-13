package net.minecraft.world.level.block;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HoneyBlock extends HalfTransparentBlock {
    protected static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 15.0, 15.0);

    public HoneyBlock(Block.Properties param0) {
        super(param0);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPE;
    }

    @Override
    public void fallOn(Level param0, BlockPos param1, Entity param2, float param3) {
        this.doLandingParticleEffect(param0, param1, param2);
        if (param2.causeFallDamage(param3, 0.2F)) {
            param2.playSound(this.soundType.getFallSound(), this.soundType.getVolume() * 0.5F, this.soundType.getPitch() * 0.75F);
        }

    }

    @Override
    public void entityInside(BlockState param0, Level param1, BlockPos param2, Entity param3) {
        if (this.isSlidingDown(param2, param3)) {
            Vec3 var0 = param3.getDeltaMovement();
            if (var0.y < -0.05) {
                if (param3 instanceof ServerPlayer && var0.y < -0.127) {
                    CriteriaTriggers.HONEY_BLOCK_SLIDE.trigger((ServerPlayer)param3, param1.getBlockState(param2));
                }

                param3.setDeltaMovement(new Vec3(var0.x, -0.05, var0.z));
            }

            param3.fallDistance = 0.0F;
            this.doSlideDownParticleEffects(param1, param2, param3);
            if (param1.getGameTime() % 10L == 0L) {
                param3.playSound(SoundEvents.HONEY_BLOCK_SLIDE, 1.0F, 1.0F);
            }
        }

        super.entityInside(param0, param1, param2, param3);
    }

    private boolean isSlidingDown(BlockPos param0, Entity param1) {
        if (param1.onGround) {
            return false;
        } else if (param1.getY() > (double)param0.getY() + 0.9375 - 1.0E-7) {
            return false;
        } else if (param1.getDeltaMovement().y >= -0.04) {
            return false;
        } else {
            double var0 = Math.abs((double)param0.getX() + 0.5 - param1.getX());
            double var1 = Math.abs((double)param0.getZ() + 0.5 - param1.getZ());
            double var2 = 0.4375 + (double)(param1.getBbWidth() / 2.0F);
            return var0 + 1.0E-7 > var2 || var1 + 1.0E-7 > var2;
        }
    }

    private void doSlideDownParticleEffects(Level param0, BlockPos param1, Entity param2) {
        float var0 = param2.getDimensions(Pose.STANDING).width;
        this.doParticleEffects(
            param2,
            param0,
            param1,
            1,
            ((double)param0.random.nextFloat() - 0.5) * (double)var0,
            (double)(param0.random.nextFloat() / 2.0F),
            ((double)param0.random.nextFloat() - 0.5) * (double)var0,
            (double)param0.random.nextFloat() - 0.5,
            (double)(param0.random.nextFloat() - 1.0F),
            (double)param0.random.nextFloat() - 0.5
        );
    }

    private void doLandingParticleEffect(Level param0, BlockPos param1, Entity param2) {
        float var0 = param2.getDimensions(Pose.STANDING).width;
        this.doParticleEffects(
            param2,
            param0,
            param1,
            10,
            ((double)param0.random.nextFloat() - 0.5) * (double)var0,
            0.0,
            ((double)param0.random.nextFloat() - 0.5) * (double)var0,
            (double)param0.random.nextFloat() - 0.5,
            0.5,
            (double)param0.random.nextFloat() - 0.5
        );
    }

    private void doParticleEffects(
        Entity param0, Level param1, BlockPos param2, int param3, double param4, double param5, double param6, double param7, double param8, double param9
    ) {
        BlockState var0 = param1.getBlockState(new BlockPos(param2));

        for(int var1 = 0; var1 < param3; ++var1) {
            param0.level
                .addParticle(
                    new BlockParticleOption(ParticleTypes.BLOCK, var0),
                    param0.getX() + param4,
                    param0.getY() + param5,
                    param0.getZ() + param6,
                    param7,
                    param8,
                    param9
                );
        }

    }
}

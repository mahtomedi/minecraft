package net.minecraft.world.level.material;

import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class WaterFluid extends FlowingFluid {
    @Override
    public Fluid getFlowing() {
        return Fluids.FLOWING_WATER;
    }

    @Override
    public Fluid getSource() {
        return Fluids.WATER;
    }

    @Override
    public Item getBucket() {
        return Items.WATER_BUCKET;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(Level param0, BlockPos param1, FluidState param2, Random param3) {
        if (!param2.isSource() && !param2.getValue(FALLING)) {
            if (param3.nextInt(64) == 0) {
                param0.playLocalSound(
                    (double)param1.getX() + 0.5,
                    (double)param1.getY() + 0.5,
                    (double)param1.getZ() + 0.5,
                    SoundEvents.WATER_AMBIENT,
                    SoundSource.BLOCKS,
                    param3.nextFloat() * 0.25F + 0.75F,
                    param3.nextFloat() + 0.5F,
                    false
                );
            }
        } else if (param3.nextInt(10) == 0) {
            param0.addParticle(
                ParticleTypes.UNDERWATER,
                (double)param1.getX() + param3.nextDouble(),
                (double)param1.getY() + param3.nextDouble(),
                (double)param1.getZ() + param3.nextDouble(),
                0.0,
                0.0,
                0.0
            );
        }

    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    @Override
    public ParticleOptions getDripParticle() {
        return ParticleTypes.DRIPPING_WATER;
    }

    @Override
    protected boolean canConvertToSource() {
        return true;
    }

    @Override
    protected void beforeDestroyingBlock(LevelAccessor param0, BlockPos param1, BlockState param2) {
        BlockEntity var0 = param2.hasBlockEntity() ? param0.getBlockEntity(param1) : null;
        Block.dropResources(param2, param0, param1, var0);
    }

    @Override
    public int getSlopeFindDistance(LevelReader param0) {
        return 4;
    }

    @Override
    public BlockState createLegacyBlock(FluidState param0) {
        return Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, Integer.valueOf(getLegacyLevel(param0)));
    }

    @Override
    public boolean isSame(Fluid param0) {
        return param0 == Fluids.WATER || param0 == Fluids.FLOWING_WATER;
    }

    @Override
    public int getDropOff(LevelReader param0) {
        return 1;
    }

    @Override
    public int getTickDelay(LevelReader param0) {
        return 5;
    }

    @Override
    public boolean canBeReplacedWith(FluidState param0, BlockGetter param1, BlockPos param2, Fluid param3, Direction param4) {
        return param4 == Direction.DOWN && !param3.is(FluidTags.WATER);
    }

    @Override
    protected float getExplosionResistance() {
        return 100.0F;
    }

    @Override
    public Optional<SoundEvent> getPickupSound() {
        return Optional.of(SoundEvents.BUCKET_FILL);
    }

    public static class Flowing extends WaterFluid {
        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> param0) {
            super.createFluidStateDefinition(param0);
            param0.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState param0) {
            return param0.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState param0) {
            return false;
        }
    }

    public static class Source extends WaterFluid {
        @Override
        public int getAmount(FluidState param0) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState param0) {
            return true;
        }
    }
}

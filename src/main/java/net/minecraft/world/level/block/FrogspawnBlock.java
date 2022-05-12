package net.minecraft.world.level.block;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.frog.Tadpole;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FrogspawnBlock extends Block {
    private static final int MIN_TADPOLES_SPAWN = 2;
    private static final int MAX_TADPOLES_SPAWN = 5;
    private static final int DEFAULT_MIN_HATCH_TICK_DELAY = 3600;
    private static final int DEFAULT_MAX_HATCH_TICK_DELAY = 12000;
    protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 1.5, 16.0);
    private static int minHatchTickDelay = 3600;
    private static int maxHatchTickDelay = 12000;

    public FrogspawnBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPE;
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        return mayPlaceOn(param1, param2.below());
    }

    @Override
    public void onPlace(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        param1.scheduleTick(param2, this, getFrogspawnHatchDelay(param1.getRandom()));
    }

    private static int getFrogspawnHatchDelay(RandomSource param0) {
        return param0.nextInt(minHatchTickDelay, maxHatchTickDelay);
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        return !this.canSurvive(param0, param3, param4) ? Blocks.AIR.defaultBlockState() : super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, RandomSource param3) {
        if (!this.canSurvive(param0, param1, param2)) {
            this.destroyBlock(param1, param2);
        } else {
            this.hatchFrogspawn(param1, param2, param3);
        }
    }

    @Override
    public void entityInside(BlockState param0, Level param1, BlockPos param2, Entity param3) {
        if (param3.getType().equals(EntityType.FALLING_BLOCK)) {
            this.destroyBlock(param1, param2);
        }

    }

    private static boolean mayPlaceOn(BlockGetter param0, BlockPos param1) {
        FluidState var0 = param0.getFluidState(param1);
        FluidState var1 = param0.getFluidState(param1.above());
        return var0.getType() == Fluids.WATER && var1.getType() == Fluids.EMPTY;
    }

    private void hatchFrogspawn(ServerLevel param0, BlockPos param1, RandomSource param2) {
        this.destroyBlock(param0, param1);
        param0.playSound(null, param1, SoundEvents.FROGSPAWN_HATCH, SoundSource.BLOCKS, 1.0F, 1.0F);
        this.spawnTadpoles(param0, param1, param2);
    }

    private void destroyBlock(Level param0, BlockPos param1) {
        param0.destroyBlock(param1, false);
    }

    private void spawnTadpoles(ServerLevel param0, BlockPos param1, RandomSource param2) {
        int var0 = param2.nextInt(2, 6);

        for(int var1 = 1; var1 <= var0; ++var1) {
            Tadpole var2 = EntityType.TADPOLE.create(param0);
            double var3 = (double)param1.getX() + this.getRandomTadpolePositionOffset(param2);
            double var4 = (double)param1.getZ() + this.getRandomTadpolePositionOffset(param2);
            int var5 = param2.nextInt(1, 361);
            var2.moveTo(var3, (double)param1.getY() - 0.5, var4, (float)var5, 0.0F);
            var2.setPersistenceRequired();
            param0.addFreshEntity(var2);
        }

    }

    private double getRandomTadpolePositionOffset(RandomSource param0) {
        double var0 = (double)(Tadpole.HITBOX_WIDTH / 2.0F);
        return Mth.clamp(param0.nextDouble(), var0, 1.0 - var0);
    }

    @VisibleForTesting
    public static void setHatchDelay(int param0, int param1) {
        minHatchTickDelay = param0;
        maxHatchTickDelay = param1;
    }

    @VisibleForTesting
    public static void setDefaultHatchDelay() {
        minHatchTickDelay = 3600;
        maxHatchTickDelay = 12000;
    }
}

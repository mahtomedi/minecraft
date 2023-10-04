package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public abstract class FallingBlock extends Block implements Fallable {
    public FallingBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    protected abstract MapCodec<? extends FallingBlock> codec();

    @Override
    public void onPlace(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        param1.scheduleTick(param2, this, this.getDelayAfterPlace());
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        param3.scheduleTick(param4, this, this.getDelayAfterPlace());
        return super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, RandomSource param3) {
        if (isFree(param1.getBlockState(param2.below())) && param2.getY() >= param1.getMinBuildHeight()) {
            FallingBlockEntity var0 = FallingBlockEntity.fall(param1, param2, param0);
            this.falling(var0);
        }
    }

    protected void falling(FallingBlockEntity param0) {
    }

    protected int getDelayAfterPlace() {
        return 2;
    }

    public static boolean isFree(BlockState param0) {
        return param0.isAir() || param0.is(BlockTags.FIRE) || param0.liquid() || param0.canBeReplaced();
    }

    @Override
    public void animateTick(BlockState param0, Level param1, BlockPos param2, RandomSource param3) {
        if (param3.nextInt(16) == 0) {
            BlockPos var0 = param2.below();
            if (isFree(param1.getBlockState(var0))) {
                ParticleUtils.spawnParticleBelow(param1, param2, param3, new BlockParticleOption(ParticleTypes.FALLING_DUST, param0));
            }
        }

    }

    public int getDustColor(BlockState param0, BlockGetter param1, BlockPos param2) {
        return -16777216;
    }
}

package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FallingBlock extends Block {
    public FallingBlock(Block.Properties param0) {
        super(param0);
    }

    @Override
    public void onPlace(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        param1.getBlockTicks().scheduleTick(param2, this, this.getTickDelay(param1));
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        param3.getBlockTicks().scheduleTick(param4, this, this.getTickDelay(param3));
        return super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        if (isFree(param1.getBlockState(param2.below())) && param2.getY() >= 0) {
            FallingBlockEntity var0 = new FallingBlockEntity(
                param1, (double)param2.getX() + 0.5, (double)param2.getY(), (double)param2.getZ() + 0.5, param1.getBlockState(param2)
            );
            this.falling(var0);
            param1.addFreshEntity(var0);
        }
    }

    protected void falling(FallingBlockEntity param0) {
    }

    @Override
    public int getTickDelay(LevelReader param0) {
        return 2;
    }

    public static boolean isFree(BlockState param0) {
        Block var0 = param0.getBlock();
        Material var1 = param0.getMaterial();
        return param0.isAir() || var0 == Blocks.FIRE || var1.isLiquid() || var1.isReplaceable();
    }

    public void onLand(Level param0, BlockPos param1, BlockState param2, BlockState param3) {
    }

    public void onBroken(Level param0, BlockPos param1) {
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(BlockState param0, Level param1, BlockPos param2, Random param3) {
        if (param3.nextInt(16) == 0) {
            BlockPos var0 = param2.below();
            if (isFree(param1.getBlockState(var0))) {
                double var1 = (double)param2.getX() + (double)param3.nextFloat();
                double var2 = (double)param2.getY() - 0.05;
                double var3 = (double)param2.getZ() + (double)param3.nextFloat();
                param1.addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, param0), var1, var2, var3, 0.0, 0.0, 0.0);
            }
        }

    }

    @OnlyIn(Dist.CLIENT)
    public int getDustColor(BlockState param0) {
        return -16777216;
    }
}

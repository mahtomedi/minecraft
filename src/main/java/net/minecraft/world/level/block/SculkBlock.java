package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public class SculkBlock extends DropExperienceBlock implements SculkBehaviour {
    private static final int VARYING_GROWTH_RATE_DISTANCE_SQUARED = 400;

    public SculkBlock(BlockBehaviour.Properties param0) {
        super(param0, ConstantInt.of(1));
    }

    @Override
    public int attemptUseCharge(SculkSpreader.ChargeCursor param0, Level param1, BlockPos param2, Random param3) {
        int var0 = param0.getCharge();
        if (var0 != 0 && param3.nextInt(10) == 0) {
            BlockPos var1 = param0.getPos();
            boolean var2 = var1.closerThan(param2, 4.0);
            if (!var2 && canPlaceGrowth(param1, var1)) {
                if (param3.nextInt(10) < var0) {
                    param1.setBlock(var1.above(), Blocks.SCULK_SENSOR.defaultBlockState(), 3);
                    param1.playSound(null, var1, SoundEvents.SCULK_SENSOR_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                }

                return Math.max(0, var0 - 10);
            } else {
                return param3.nextInt(5) != 0 ? var0 : var0 - (var2 ? 1 : getDecayPenalty(var1, param2, var0));
            }
        } else {
            return var0;
        }
    }

    private static int getDecayPenalty(BlockPos param0, BlockPos param1, int param2) {
        float var0 = (float)Math.sqrt(param0.distSqr(param1)) - 4.0F;
        float var1 = Math.min(1.0F, var0 * var0 / 400.0F);
        return Math.max(1, (int)((float)param2 * var1 * 0.5F));
    }

    private static boolean canPlaceGrowth(Level param0, BlockPos param1) {
        BlockState var0 = param0.getBlockState(param1.above());
        if (var0.isAir() || var0.is(Blocks.WATER) && var0.getFluidState().is(Fluids.WATER)) {
            int var1 = 0;

            for(BlockPos var2 : BlockPos.betweenClosed(param1.offset(-4, 0, -4), param1.offset(4, 2, 4))) {
                BlockState var3 = param0.getBlockState(var2);
                if (var3.is(Blocks.SCULK_SENSOR)) {
                    ++var1;
                }

                if (var1 > 2) {
                    return false;
                }
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean canChangeBlockStateOnSpread() {
        return false;
    }
}

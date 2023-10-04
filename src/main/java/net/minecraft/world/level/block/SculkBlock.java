package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;

public class SculkBlock extends DropExperienceBlock implements SculkBehaviour {
    public static final MapCodec<SculkBlock> CODEC = simpleCodec(SculkBlock::new);

    @Override
    public MapCodec<SculkBlock> codec() {
        return CODEC;
    }

    public SculkBlock(BlockBehaviour.Properties param0) {
        super(ConstantInt.of(1), param0);
    }

    @Override
    public int attemptUseCharge(
        SculkSpreader.ChargeCursor param0, LevelAccessor param1, BlockPos param2, RandomSource param3, SculkSpreader param4, boolean param5
    ) {
        int var0 = param0.getCharge();
        if (var0 != 0 && param3.nextInt(param4.chargeDecayRate()) == 0) {
            BlockPos var1 = param0.getPos();
            boolean var2 = var1.closerThan(param2, (double)param4.noGrowthRadius());
            if (!var2 && canPlaceGrowth(param1, var1)) {
                int var3 = param4.growthSpawnCost();
                if (param3.nextInt(var3) < var0) {
                    BlockPos var4 = var1.above();
                    BlockState var5 = this.getRandomGrowthState(param1, var4, param3, param4.isWorldGeneration());
                    param1.setBlock(var4, var5, 3);
                    param1.playSound(null, var1, var5.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
                }

                return Math.max(0, var0 - var3);
            } else {
                return param3.nextInt(param4.additionalDecayRate()) != 0 ? var0 : var0 - (var2 ? 1 : getDecayPenalty(param4, var1, param2, var0));
            }
        } else {
            return var0;
        }
    }

    private static int getDecayPenalty(SculkSpreader param0, BlockPos param1, BlockPos param2, int param3) {
        int var0 = param0.noGrowthRadius();
        float var1 = Mth.square((float)Math.sqrt(param1.distSqr(param2)) - (float)var0);
        int var2 = Mth.square(24 - var0);
        float var3 = Math.min(1.0F, var1 / (float)var2);
        return Math.max(1, (int)((float)param3 * var3 * 0.5F));
    }

    private BlockState getRandomGrowthState(LevelAccessor param0, BlockPos param1, RandomSource param2, boolean param3) {
        BlockState var0;
        if (param2.nextInt(11) == 0) {
            var0 = Blocks.SCULK_SHRIEKER.defaultBlockState().setValue(SculkShriekerBlock.CAN_SUMMON, Boolean.valueOf(param3));
        } else {
            var0 = Blocks.SCULK_SENSOR.defaultBlockState();
        }

        return var0.hasProperty(BlockStateProperties.WATERLOGGED) && !param0.getFluidState(param1).isEmpty()
            ? var0.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(true))
            : var0;
    }

    private static boolean canPlaceGrowth(LevelAccessor param0, BlockPos param1) {
        BlockState var0 = param0.getBlockState(param1.above());
        if (var0.isAir() || var0.is(Blocks.WATER) && var0.getFluidState().is(Fluids.WATER)) {
            int var1 = 0;

            for(BlockPos var2 : BlockPos.betweenClosed(param1.offset(-4, 0, -4), param1.offset(4, 2, 4))) {
                BlockState var3 = param0.getBlockState(var2);
                if (var3.is(Blocks.SCULK_SENSOR) || var3.is(Blocks.SCULK_SHRIEKER)) {
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

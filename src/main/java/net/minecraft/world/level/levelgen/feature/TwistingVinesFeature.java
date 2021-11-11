package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TwistingVinesConfig;

public class TwistingVinesFeature extends Feature<TwistingVinesConfig> {
    public TwistingVinesFeature(Codec<TwistingVinesConfig> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<TwistingVinesConfig> param0) {
        WorldGenLevel var0 = param0.level();
        BlockPos var1 = param0.origin();
        if (isInvalidPlacementLocation(var0, var1)) {
            return false;
        } else {
            Random var2 = param0.random();
            TwistingVinesConfig var3 = param0.config();
            int var4 = var3.spreadWidth();
            int var5 = var3.spreadHeight();
            int var6 = var3.maxHeight();
            BlockPos.MutableBlockPos var7 = new BlockPos.MutableBlockPos();

            for(int var8 = 0; var8 < var4 * var4; ++var8) {
                var7.set(var1).move(Mth.nextInt(var2, -var4, var4), Mth.nextInt(var2, -var5, var5), Mth.nextInt(var2, -var4, var4));
                if (findFirstAirBlockAboveGround(var0, var7) && !isInvalidPlacementLocation(var0, var7)) {
                    int var9 = Mth.nextInt(var2, 1, var6);
                    if (var2.nextInt(6) == 0) {
                        var9 *= 2;
                    }

                    if (var2.nextInt(5) == 0) {
                        var9 = 1;
                    }

                    int var10 = 17;
                    int var11 = 25;
                    placeWeepingVinesColumn(var0, var2, var7, var9, 17, 25);
                }
            }

            return true;
        }
    }

    private static boolean findFirstAirBlockAboveGround(LevelAccessor param0, BlockPos.MutableBlockPos param1) {
        do {
            param1.move(0, -1, 0);
            if (param0.isOutsideBuildHeight(param1)) {
                return false;
            }
        } while(param0.getBlockState(param1).isAir());

        param1.move(0, 1, 0);
        return true;
    }

    public static void placeWeepingVinesColumn(LevelAccessor param0, Random param1, BlockPos.MutableBlockPos param2, int param3, int param4, int param5) {
        for(int var0 = 1; var0 <= param3; ++var0) {
            if (param0.isEmptyBlock(param2)) {
                if (var0 == param3 || !param0.isEmptyBlock(param2.above())) {
                    param0.setBlock(
                        param2,
                        Blocks.TWISTING_VINES.defaultBlockState().setValue(GrowingPlantHeadBlock.AGE, Integer.valueOf(Mth.nextInt(param1, param4, param5))),
                        2
                    );
                    break;
                }

                param0.setBlock(param2, Blocks.TWISTING_VINES_PLANT.defaultBlockState(), 2);
            }

            param2.move(Direction.UP);
        }

    }

    private static boolean isInvalidPlacementLocation(LevelAccessor param0, BlockPos param1) {
        if (!param0.isEmptyBlock(param1)) {
            return true;
        } else {
            BlockState var0 = param0.getBlockState(param1.below());
            return !var0.is(Blocks.NETHERRACK) && !var0.is(Blocks.WARPED_NYLIUM) && !var0.is(Blocks.WARPED_WART_BLOCK);
        }
    }
}

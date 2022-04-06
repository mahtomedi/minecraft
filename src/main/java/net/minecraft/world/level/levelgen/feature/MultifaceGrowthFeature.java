package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.MultifaceGrowthConfiguration;

public class MultifaceGrowthFeature extends Feature<MultifaceGrowthConfiguration> {
    public MultifaceGrowthFeature(Codec<MultifaceGrowthConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<MultifaceGrowthConfiguration> param0) {
        WorldGenLevel var0 = param0.level();
        BlockPos var1 = param0.origin();
        RandomSource var2 = param0.random();
        MultifaceGrowthConfiguration var3 = param0.config();
        if (!isAirOrWater(var0.getBlockState(var1))) {
            return false;
        } else {
            List<Direction> var4 = getShuffledDirections(var3, var2);
            if (placeGrowthIfPossible(var0, var1, var0.getBlockState(var1), var3, var2, var4)) {
                return true;
            } else {
                BlockPos.MutableBlockPos var5 = var1.mutable();

                for(Direction var6 : var4) {
                    var5.set(var1);
                    List<Direction> var7 = getShuffledDirectionsExcept(var3, var2, var6.getOpposite());

                    for(int var8 = 0; var8 < var3.searchRange; ++var8) {
                        var5.setWithOffset(var1, var6);
                        BlockState var9 = var0.getBlockState(var5);
                        if (!isAirOrWater(var9) && !var9.is(var3.placeBlock)) {
                            break;
                        }

                        if (placeGrowthIfPossible(var0, var5, var9, var3, var2, var7)) {
                            return true;
                        }
                    }
                }

                return false;
            }
        }
    }

    public static boolean placeGrowthIfPossible(
        WorldGenLevel param0, BlockPos param1, BlockState param2, MultifaceGrowthConfiguration param3, RandomSource param4, List<Direction> param5
    ) {
        BlockPos.MutableBlockPos var0 = param1.mutable();

        for(Direction var1 : param5) {
            BlockState var2 = param0.getBlockState(var0.setWithOffset(param1, var1));
            if (var2.is(param3.canBePlacedOn)) {
                BlockState var3 = param3.placeBlock.getStateForPlacement(param2, param0, param1, var1);
                if (var3 == null) {
                    return false;
                }

                param0.setBlock(param1, var3, 3);
                param0.getChunk(param1).markPosForPostprocessing(param1);
                if (param4.nextFloat() < param3.chanceOfSpreading) {
                    param3.placeBlock.getSpreader().spreadFromFaceTowardRandomDirection(var3, param0, param1, var1, param4, true);
                }

                return true;
            }
        }

        return false;
    }

    public static List<Direction> getShuffledDirections(MultifaceGrowthConfiguration param0, RandomSource param1) {
        return Util.shuffledCopy(param0.validDirections, param1);
    }

    public static List<Direction> getShuffledDirectionsExcept(MultifaceGrowthConfiguration param0, RandomSource param1, Direction param2) {
        return Util.shuffledCopy(param0.validDirections.stream().filter(param1x -> param1x != param2).collect(Collectors.toList()), param1);
    }

    private static boolean isAirOrWater(BlockState param0) {
        return param0.isAir() || param0.is(Blocks.WATER);
    }
}

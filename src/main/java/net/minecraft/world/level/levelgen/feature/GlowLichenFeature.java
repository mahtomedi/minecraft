package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GlowLichenBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.GlowLichenConfiguration;

public class GlowLichenFeature extends Feature<GlowLichenConfiguration> {
    public GlowLichenFeature(Codec<GlowLichenConfiguration> param0) {
        super(param0);
    }

    public boolean place(WorldGenLevel param0, ChunkGenerator param1, Random param2, BlockPos param3, GlowLichenConfiguration param4) {
        if (!isAirOrWater(param0.getBlockState(param3))) {
            return false;
        } else {
            List<Direction> var0 = getShuffledDirections(param4, param2);
            if (placeGlowLichenIfPossible(param0, param3, param0.getBlockState(param3), param4, param2, var0)) {
                return true;
            } else {
                BlockPos.MutableBlockPos var1 = param3.mutable();

                for(Direction var2 : var0) {
                    var1.set(param3);
                    List<Direction> var3 = getShuffledDirectionsExcept(param4, param2, var2.getOpposite());

                    for(int var4 = 0; var4 < param4.searchRange; ++var4) {
                        var1.setWithOffset(param3, var2);
                        BlockState var5 = param0.getBlockState(var1);
                        if (!isAirOrWater(var5) && !var5.is(Blocks.GLOW_LICHEN)) {
                            break;
                        }

                        if (placeGlowLichenIfPossible(param0, var1, var5, param4, param2, var3)) {
                            return true;
                        }
                    }
                }

                return false;
            }
        }
    }

    public static boolean placeGlowLichenIfPossible(
        WorldGenLevel param0, BlockPos param1, BlockState param2, GlowLichenConfiguration param3, Random param4, List<Direction> param5
    ) {
        BlockPos.MutableBlockPos var0 = param1.mutable();

        for(Direction var1 : param5) {
            BlockState var2 = param0.getBlockState(var0.setWithOffset(param1, var1));
            if (param3.canBePlacedOn(var2.getBlock())) {
                GlowLichenBlock var3 = (GlowLichenBlock)Blocks.GLOW_LICHEN;
                BlockState var4 = var3.getStateForPlacement(param2, param0, param1, var1);
                if (var4 == null) {
                    return false;
                }

                param0.setBlock(param1, var4, 3);
                if (param4.nextFloat() < param3.chanceOfSpreading) {
                    var3.spreadFromFaceTowardRandomDirection(var4, param0, param1, var1, param4);
                }

                return true;
            }
        }

        return false;
    }

    public static List<Direction> getShuffledDirections(GlowLichenConfiguration param0, Random param1) {
        List<Direction> var0 = Lists.newArrayList(param0.validDirections);
        Collections.shuffle(var0, param1);
        return var0;
    }

    public static List<Direction> getShuffledDirectionsExcept(GlowLichenConfiguration param0, Random param1, Direction param2) {
        List<Direction> var0 = param0.validDirections.stream().filter(param1x -> param1x != param2).collect(Collectors.toList());
        Collections.shuffle(var0, param1);
        return var0;
    }

    private static boolean isAirOrWater(BlockState param0) {
        return param0.isAir() || param0.is(Blocks.WATER);
    }
}

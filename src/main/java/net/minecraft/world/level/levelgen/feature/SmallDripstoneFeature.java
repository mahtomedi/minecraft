package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.SmallDripstoneConfiguration;

public class SmallDripstoneFeature extends Feature<SmallDripstoneConfiguration> {
    public SmallDripstoneFeature(Codec<SmallDripstoneConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<SmallDripstoneConfiguration> param0) {
        WorldGenLevel var0 = param0.level();
        BlockPos var1 = param0.origin();
        Random var2 = param0.random();
        SmallDripstoneConfiguration var3 = param0.config();
        if (!DripstoneUtils.isEmptyOrWater(var0, var1)) {
            return false;
        } else {
            int var4 = Mth.randomBetweenInclusive(var2, 1, var3.maxPlacements);
            boolean var5 = false;

            for(int var6 = 0; var6 < var4; ++var6) {
                BlockPos var7 = randomOffset(var2, var1, var3);
                if (searchAndTryToPlaceDripstone(var0, var2, var7, var3)) {
                    var5 = true;
                }
            }

            return var5;
        }
    }

    private static boolean searchAndTryToPlaceDripstone(WorldGenLevel param0, Random param1, BlockPos param2, SmallDripstoneConfiguration param3) {
        Direction var0 = Direction.getRandom(param1);
        Direction var1 = param1.nextBoolean() ? Direction.UP : Direction.DOWN;
        BlockPos.MutableBlockPos var2 = param2.mutable();

        for(int var3 = 0; var3 < param3.emptySpaceSearchRadius; ++var3) {
            if (!DripstoneUtils.isEmptyOrWater(param0, var2)) {
                return false;
            }

            if (tryToPlaceDripstone(param0, param1, var2, var1, param3)) {
                return true;
            }

            if (tryToPlaceDripstone(param0, param1, var2, var1.getOpposite(), param3)) {
                return true;
            }

            var2.move(var0);
        }

        return false;
    }

    private static boolean tryToPlaceDripstone(WorldGenLevel param0, Random param1, BlockPos param2, Direction param3, SmallDripstoneConfiguration param4) {
        if (!DripstoneUtils.isEmptyOrWater(param0, param2)) {
            return false;
        } else {
            BlockPos var0 = param2.relative(param3.getOpposite());
            BlockState var1 = param0.getBlockState(var0);
            if (!DripstoneUtils.isDripstoneBase(var1)) {
                return false;
            } else {
                createPatchOfDripstoneBlocks(param0, param1, var0);
                int var2 = param1.nextFloat() < param4.chanceOfTallerDripstone && DripstoneUtils.isEmptyOrWater(param0, param2.relative(param3)) ? 2 : 1;
                DripstoneUtils.growPointedDripstone(param0, param2, param3, var2, false);
                return true;
            }
        }
    }

    private static void createPatchOfDripstoneBlocks(WorldGenLevel param0, Random param1, BlockPos param2) {
        DripstoneUtils.placeDripstoneBlockIfPossible(param0, param2);

        for(Direction var0 : Direction.Plane.HORIZONTAL) {
            if (!(param1.nextFloat() < 0.3F)) {
                BlockPos var1 = param2.relative(var0);
                DripstoneUtils.placeDripstoneBlockIfPossible(param0, var1);
                if (!param1.nextBoolean()) {
                    BlockPos var2 = var1.relative(Direction.getRandom(param1));
                    DripstoneUtils.placeDripstoneBlockIfPossible(param0, var2);
                    if (!param1.nextBoolean()) {
                        BlockPos var3 = var2.relative(Direction.getRandom(param1));
                        DripstoneUtils.placeDripstoneBlockIfPossible(param0, var3);
                    }
                }
            }
        }

    }

    private static BlockPos randomOffset(Random param0, BlockPos param1, SmallDripstoneConfiguration param2) {
        return param1.offset(
            Mth.randomBetweenInclusive(param0, -param2.maxOffsetFromOrigin, param2.maxOffsetFromOrigin),
            Mth.randomBetweenInclusive(param0, -param2.maxOffsetFromOrigin, param2.maxOffsetFromOrigin),
            Mth.randomBetweenInclusive(param0, -param2.maxOffsetFromOrigin, param2.maxOffsetFromOrigin)
        );
    }
}

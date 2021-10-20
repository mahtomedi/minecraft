package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.feature.configurations.PointedDripstoneConfiguration;

public class PointedDripstoneFeature extends Feature<PointedDripstoneConfiguration> {
    public PointedDripstoneFeature(Codec<PointedDripstoneConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<PointedDripstoneConfiguration> param0) {
        LevelAccessor var0 = param0.level();
        BlockPos var1 = param0.origin();
        Random var2 = param0.random();
        PointedDripstoneConfiguration var3 = param0.config();
        Optional<Direction> var4 = getTipDirection(var0, var1, var2);
        if (var4.isEmpty()) {
            return false;
        } else {
            BlockPos var5 = var1.relative(var4.get().getOpposite());
            createPatchOfDripstoneBlocks(var0, var2, var5, var3);
            int var6 = var2.nextFloat() < var3.chanceOfTallerDripstone && DripstoneUtils.isEmptyOrWater(var0.getBlockState(var1.relative(var4.get()))) ? 2 : 1;
            DripstoneUtils.growPointedDripstone(var0, var1, var4.get(), var6, false);
            return true;
        }
    }

    private static Optional<Direction> getTipDirection(LevelAccessor param0, BlockPos param1, Random param2) {
        boolean var0 = DripstoneUtils.isDripstoneBase(param0.getBlockState(param1.above()));
        boolean var1 = DripstoneUtils.isDripstoneBase(param0.getBlockState(param1.below()));
        if (var0 && var1) {
            return Optional.of(param2.nextBoolean() ? Direction.DOWN : Direction.UP);
        } else if (var0) {
            return Optional.of(Direction.DOWN);
        } else {
            return var1 ? Optional.of(Direction.UP) : Optional.empty();
        }
    }

    private static void createPatchOfDripstoneBlocks(LevelAccessor param0, Random param1, BlockPos param2, PointedDripstoneConfiguration param3) {
        DripstoneUtils.placeDripstoneBlockIfPossible(param0, param2);

        for(Direction var0 : Direction.Plane.HORIZONTAL) {
            if (!(param1.nextFloat() > param3.chanceOfDirectionalSpread)) {
                BlockPos var1 = param2.relative(var0);
                DripstoneUtils.placeDripstoneBlockIfPossible(param0, var1);
                if (!(param1.nextFloat() > param3.chanceOfSpreadRadius2)) {
                    BlockPos var2 = var1.relative(Direction.getRandom(param1));
                    DripstoneUtils.placeDripstoneBlockIfPossible(param0, var2);
                    if (!(param1.nextFloat() > param3.chanceOfSpreadRadius3)) {
                        BlockPos var3 = var2.relative(Direction.getRandom(param1));
                        DripstoneUtils.placeDripstoneBlockIfPossible(param0, var3);
                    }
                }
            }
        }

    }
}

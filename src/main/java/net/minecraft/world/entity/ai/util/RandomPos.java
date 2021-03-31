package net.minecraft.world.entity.ai.util;

import com.google.common.annotations.VisibleForTesting;
import java.util.Random;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

public class RandomPos {
    private static final int RANDOM_POS_ATTEMPTS = 10;

    public static BlockPos generateRandomDirection(Random param0, int param1, int param2) {
        int var0 = param0.nextInt(2 * param1 + 1) - param1;
        int var1 = param0.nextInt(2 * param2 + 1) - param2;
        int var2 = param0.nextInt(2 * param1 + 1) - param1;
        return new BlockPos(var0, var1, var2);
    }

    @Nullable
    public static BlockPos generateRandomDirectionWithinRadians(Random param0, int param1, int param2, int param3, double param4, double param5, double param6) {
        double var0 = Mth.atan2(param5, param4) - (float) (Math.PI / 2);
        double var1 = var0 + (double)(2.0F * param0.nextFloat() - 1.0F) * param6;
        double var2 = Math.sqrt(param0.nextDouble()) * (double)Mth.SQRT_OF_TWO * (double)param1;
        double var3 = -var2 * Math.sin(var1);
        double var4 = var2 * Math.cos(var1);
        if (!(Math.abs(var3) > (double)param1) && !(Math.abs(var4) > (double)param1)) {
            int var5 = param0.nextInt(2 * param2 + 1) - param2 + param3;
            return new BlockPos(var3, (double)var5, var4);
        } else {
            return null;
        }
    }

    @VisibleForTesting
    public static BlockPos moveUpOutOfSolid(BlockPos param0, int param1, Predicate<BlockPos> param2) {
        if (!param2.test(param0)) {
            return param0;
        } else {
            BlockPos var0 = param0.above();

            while(var0.getY() < param1 && param2.test(var0)) {
                var0 = var0.above();
            }

            return var0;
        }
    }

    @VisibleForTesting
    public static BlockPos moveUpToAboveSolid(BlockPos param0, int param1, int param2, Predicate<BlockPos> param3) {
        if (param1 < 0) {
            throw new IllegalArgumentException("aboveSolidAmount was " + param1 + ", expected >= 0");
        } else if (!param3.test(param0)) {
            return param0;
        } else {
            BlockPos var0 = param0.above();

            while(var0.getY() < param2 && param3.test(var0)) {
                var0 = var0.above();
            }

            BlockPos var1;
            BlockPos var2;
            for(var1 = var0; var1.getY() < param2 && var1.getY() - var0.getY() < param1; var1 = var2) {
                var2 = var1.above();
                if (param3.test(var2)) {
                    break;
                }
            }

            return var1;
        }
    }

    @Nullable
    public static Vec3 generateRandomPos(PathfinderMob param0, Supplier<BlockPos> param1) {
        return generateRandomPos(param1, param0::getWalkTargetValue);
    }

    @Nullable
    public static Vec3 generateRandomPos(Supplier<BlockPos> param0, ToDoubleFunction<BlockPos> param1) {
        double var0 = Double.NEGATIVE_INFINITY;
        BlockPos var1 = null;

        for(int var2 = 0; var2 < 10; ++var2) {
            BlockPos var3 = param0.get();
            if (var3 != null) {
                double var4 = param1.applyAsDouble(var3);
                if (var4 > var0) {
                    var0 = var4;
                    var1 = var3;
                }
            }
        }

        return var1 != null ? Vec3.atBottomCenterOf(var1) : null;
    }

    public static BlockPos generateRandomPosTowardDirection(PathfinderMob param0, int param1, Random param2, BlockPos param3) {
        int var0 = param3.getX();
        int var1 = param3.getZ();
        if (param0.hasRestriction() && param1 > 1) {
            BlockPos var2 = param0.getRestrictCenter();
            if (param0.getX() > (double)var2.getX()) {
                var0 -= param2.nextInt(param1 / 2);
            } else {
                var0 += param2.nextInt(param1 / 2);
            }

            if (param0.getZ() > (double)var2.getZ()) {
                var1 -= param2.nextInt(param1 / 2);
            } else {
                var1 += param2.nextInt(param1 / 2);
            }
        }

        return new BlockPos((double)var0 + param0.getX(), (double)param3.getY() + param0.getY(), (double)var1 + param0.getZ());
    }
}

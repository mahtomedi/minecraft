package net.minecraft.world.entity.ai.util;

import java.util.Random;
import java.util.function.ToDoubleFunction;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.phys.Vec3;

public class RandomPos {
    @Nullable
    public static Vec3 getPos(PathfinderMob param0, int param1, int param2) {
        return generateRandomPos(param0, param1, param2, null);
    }

    @Nullable
    public static Vec3 getLandPos(PathfinderMob param0, int param1, int param2) {
        return getLandPos(param0, param1, param2, param0::getWalkTargetValue);
    }

    @Nullable
    public static Vec3 getLandPos(PathfinderMob param0, int param1, int param2, ToDoubleFunction<BlockPos> param3) {
        return generateRandomPos(param0, param1, param2, null, false, 0.0, param3);
    }

    @Nullable
    public static Vec3 getPosTowards(PathfinderMob param0, int param1, int param2, Vec3 param3) {
        Vec3 var0 = param3.subtract(param0.x, param0.y, param0.z);
        return generateRandomPos(param0, param1, param2, var0);
    }

    @Nullable
    public static Vec3 getPosTowards(PathfinderMob param0, int param1, int param2, Vec3 param3, double param4) {
        Vec3 var0 = param3.subtract(param0.x, param0.y, param0.z);
        return generateRandomPos(param0, param1, param2, var0, true, param4, param0::getWalkTargetValue);
    }

    @Nullable
    public static Vec3 getLandPosAvoid(PathfinderMob param0, int param1, int param2, Vec3 param3) {
        Vec3 var0 = new Vec3(param0.x, param0.y, param0.z).subtract(param3);
        return generateRandomPos(param0, param1, param2, var0, false, (float) (Math.PI / 2), param0::getWalkTargetValue);
    }

    @Nullable
    public static Vec3 getPosAvoid(PathfinderMob param0, int param1, int param2, Vec3 param3) {
        Vec3 var0 = new Vec3(param0.x, param0.y, param0.z).subtract(param3);
        return generateRandomPos(param0, param1, param2, var0);
    }

    @Nullable
    private static Vec3 generateRandomPos(PathfinderMob param0, int param1, int param2, @Nullable Vec3 param3) {
        return generateRandomPos(param0, param1, param2, param3, true, (float) (Math.PI / 2), param0::getWalkTargetValue);
    }

    @Nullable
    private static Vec3 generateRandomPos(
        PathfinderMob param0, int param1, int param2, @Nullable Vec3 param3, boolean param4, double param5, ToDoubleFunction<BlockPos> param6
    ) {
        PathNavigation var0 = param0.getNavigation();
        Random var1 = param0.getRandom();
        boolean var2;
        if (param0.hasRestriction()) {
            var2 = param0.getRestrictCenter().closerThan(param0.position(), (double)(param0.getRestrictRadius() + (float)param1) + 1.0);
        } else {
            var2 = false;
        }

        boolean var4 = false;
        double var5 = Double.NEGATIVE_INFINITY;
        BlockPos var6 = new BlockPos(param0);

        for(int var7 = 0; var7 < 10; ++var7) {
            BlockPos var8 = getRandomDelta(var1, param1, param2, param3, param5);
            if (var8 != null) {
                int var9 = var8.getX();
                int var10 = var8.getY();
                int var11 = var8.getZ();
                if (param0.hasRestriction() && param1 > 1) {
                    BlockPos var12 = param0.getRestrictCenter();
                    if (param0.x > (double)var12.getX()) {
                        var9 -= var1.nextInt(param1 / 2);
                    } else {
                        var9 += var1.nextInt(param1 / 2);
                    }

                    if (param0.z > (double)var12.getZ()) {
                        var11 -= var1.nextInt(param1 / 2);
                    } else {
                        var11 += var1.nextInt(param1 / 2);
                    }
                }

                BlockPos var13 = new BlockPos((double)var9 + param0.x, (double)var10 + param0.y, (double)var11 + param0.z);
                if ((!var2 || param0.isWithinRestriction(var13)) && var0.isStableDestination(var13)) {
                    if (!param4) {
                        var13 = moveAboveSolid(var13, param0);
                        if (isWaterDestination(var13, param0)) {
                            continue;
                        }
                    }

                    double var14 = param6.applyAsDouble(var13);
                    if (var14 > var5) {
                        var5 = var14;
                        var6 = var13;
                        var4 = true;
                    }
                }
            }
        }

        return var4 ? new Vec3(var6) : null;
    }

    @Nullable
    private static BlockPos getRandomDelta(Random param0, int param1, int param2, @Nullable Vec3 param3, double param4) {
        if (param3 != null && !(param4 >= Math.PI)) {
            double var3 = Mth.atan2(param3.z, param3.x) - (float) (Math.PI / 2);
            double var4 = var3 + (double)(2.0F * param0.nextFloat() - 1.0F) * param4;
            double var5 = Math.sqrt(param0.nextDouble()) * (double)Mth.SQRT_OF_TWO * (double)param1;
            double var6 = -var5 * Math.sin(var4);
            double var7 = var5 * Math.cos(var4);
            if (!(Math.abs(var6) > (double)param1) && !(Math.abs(var7) > (double)param1)) {
                int var8 = param0.nextInt(2 * param2 + 1) - param2;
                return new BlockPos(var6, (double)var8, var7);
            } else {
                return null;
            }
        } else {
            int var0 = param0.nextInt(2 * param1 + 1) - param1;
            int var1 = param0.nextInt(2 * param2 + 1) - param2;
            int var2 = param0.nextInt(2 * param1 + 1) - param1;
            return new BlockPos(var0, var1, var2);
        }
    }

    private static BlockPos moveAboveSolid(BlockPos param0, PathfinderMob param1) {
        if (!param1.level.getBlockState(param0).getMaterial().isSolid()) {
            return param0;
        } else {
            BlockPos var0 = param0.above();

            while(var0.getY() < param1.level.getMaxBuildHeight() && param1.level.getBlockState(var0).getMaterial().isSolid()) {
                var0 = var0.above();
            }

            return var0;
        }
    }

    private static boolean isWaterDestination(BlockPos param0, PathfinderMob param1) {
        return param1.level.getFluidState(param0).is(FluidTags.WATER);
    }
}

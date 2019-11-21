package net.minecraft.world.entity.ai.util;

import java.util.Random;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

public class RandomPos {
    @Nullable
    public static Vec3 getPos(PathfinderMob param0, int param1, int param2) {
        return generateRandomPos(param0, param1, param2, 0, null, true, (float) (Math.PI / 2), param0::getWalkTargetValue, false, 0, 0, true);
    }

    @Nullable
    public static Vec3 getAirPos(PathfinderMob param0, int param1, int param2, int param3, @Nullable Vec3 param4, double param5) {
        return generateRandomPos(param0, param1, param2, param3, param4, true, param5, param0::getWalkTargetValue, true, 0, 0, false);
    }

    @Nullable
    public static Vec3 getLandPos(PathfinderMob param0, int param1, int param2) {
        return getLandPos(param0, param1, param2, param0::getWalkTargetValue);
    }

    @Nullable
    public static Vec3 getLandPos(PathfinderMob param0, int param1, int param2, ToDoubleFunction<BlockPos> param3) {
        return generateRandomPos(param0, param1, param2, 0, null, false, 0.0, param3, true, 0, 0, true);
    }

    @Nullable
    public static Vec3 getAboveLandPos(PathfinderMob param0, int param1, int param2, Vec3 param3, float param4, int param5, int param6) {
        return generateRandomPos(param0, param1, param2, 0, param3, false, (double)param4, param0::getWalkTargetValue, true, param5, param6, true);
    }

    @Nullable
    public static Vec3 getPosTowards(PathfinderMob param0, int param1, int param2, Vec3 param3) {
        Vec3 var0 = param3.subtract(param0.getX(), param0.getY(), param0.getZ());
        return generateRandomPos(param0, param1, param2, 0, var0, true, (float) (Math.PI / 2), param0::getWalkTargetValue, false, 0, 0, true);
    }

    @Nullable
    public static Vec3 getPosTowards(PathfinderMob param0, int param1, int param2, Vec3 param3, double param4) {
        Vec3 var0 = param3.subtract(param0.getX(), param0.getY(), param0.getZ());
        return generateRandomPos(param0, param1, param2, 0, var0, true, param4, param0::getWalkTargetValue, false, 0, 0, true);
    }

    @Nullable
    public static Vec3 getAirPosTowards(PathfinderMob param0, int param1, int param2, int param3, Vec3 param4, double param5) {
        Vec3 var0 = param4.subtract(param0.getX(), param0.getY(), param0.getZ());
        return generateRandomPos(param0, param1, param2, param3, var0, false, param5, param0::getWalkTargetValue, true, 0, 0, false);
    }

    @Nullable
    public static Vec3 getPosAvoid(PathfinderMob param0, int param1, int param2, Vec3 param3) {
        Vec3 var0 = param0.position().subtract(param3);
        return generateRandomPos(param0, param1, param2, 0, var0, true, (float) (Math.PI / 2), param0::getWalkTargetValue, false, 0, 0, true);
    }

    @Nullable
    public static Vec3 getLandPosAvoid(PathfinderMob param0, int param1, int param2, Vec3 param3) {
        Vec3 var0 = param0.position().subtract(param3);
        return generateRandomPos(param0, param1, param2, 0, var0, false, (float) (Math.PI / 2), param0::getWalkTargetValue, true, 0, 0, true);
    }

    @Nullable
    private static Vec3 generateRandomPos(
        PathfinderMob param0,
        int param1,
        int param2,
        int param3,
        @Nullable Vec3 param4,
        boolean param5,
        double param6,
        ToDoubleFunction<BlockPos> param7,
        boolean param8,
        int param9,
        int param10,
        boolean param11
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
            BlockPos var8 = getRandomDelta(var1, param1, param2, param3, param4, param6);
            if (var8 != null) {
                int var9 = var8.getX();
                int var10 = var8.getY();
                int var11 = var8.getZ();
                if (param0.hasRestriction() && param1 > 1) {
                    BlockPos var12 = param0.getRestrictCenter();
                    if (param0.getX() > (double)var12.getX()) {
                        var9 -= var1.nextInt(param1 / 2);
                    } else {
                        var9 += var1.nextInt(param1 / 2);
                    }

                    if (param0.getZ() > (double)var12.getZ()) {
                        var11 -= var1.nextInt(param1 / 2);
                    } else {
                        var11 += var1.nextInt(param1 / 2);
                    }
                }

                BlockPos var13 = new BlockPos((double)var9 + param0.getX(), (double)var10 + param0.getY(), (double)var11 + param0.getZ());
                if (var13.getY() >= 0
                    && var13.getY() <= param0.level.getMaxBuildHeight()
                    && (!var2 || param0.isWithinRestriction(var13))
                    && (!param11 || var0.isStableDestination(var13))) {
                    if (param8) {
                        var13 = moveUpToAboveSolid(
                            var13,
                            var1.nextInt(param9 + 1) + param10,
                            param0.level.getMaxBuildHeight(),
                            param1x -> param0.level.getBlockState(param1x).getMaterial().isSolid()
                        );
                    }

                    if (param5 || !param0.level.getFluidState(var13).is(FluidTags.WATER)) {
                        BlockPathTypes var14 = WalkNodeEvaluator.getBlockPathTypeStatic(param0.level, var13.getX(), var13.getY(), var13.getZ());
                        if (param0.getPathfindingMalus(var14) == 0.0F) {
                            double var15 = param7.applyAsDouble(var13);
                            if (var15 > var5) {
                                var5 = var15;
                                var6 = var13;
                                var4 = true;
                            }
                        }
                    }
                }
            }
        }

        return var4 ? new Vec3(var6) : null;
    }

    @Nullable
    private static BlockPos getRandomDelta(Random param0, int param1, int param2, int param3, @Nullable Vec3 param4, double param5) {
        if (param4 != null && !(param5 >= Math.PI)) {
            double var3 = Mth.atan2(param4.z, param4.x) - (float) (Math.PI / 2);
            double var4 = var3 + (double)(2.0F * param0.nextFloat() - 1.0F) * param5;
            double var5 = Math.sqrt(param0.nextDouble()) * (double)Mth.SQRT_OF_TWO * (double)param1;
            double var6 = -var5 * Math.sin(var4);
            double var7 = var5 * Math.cos(var4);
            if (!(Math.abs(var6) > (double)param1) && !(Math.abs(var7) > (double)param1)) {
                int var8 = param0.nextInt(2 * param2 + 1) - param2 + param3;
                return new BlockPos(var6, (double)var8, var7);
            } else {
                return null;
            }
        } else {
            int var0 = param0.nextInt(2 * param1 + 1) - param1;
            int var1 = param0.nextInt(2 * param2 + 1) - param2 + param3;
            int var2 = param0.nextInt(2 * param1 + 1) - param1;
            return new BlockPos(var0, var1, var2);
        }
    }

    static BlockPos moveUpToAboveSolid(BlockPos param0, int param1, int param2, Predicate<BlockPos> param3) {
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
}

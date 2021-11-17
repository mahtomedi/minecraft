package net.minecraft.world.entity.ai.util;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

public class GoalUtils {
    public static boolean hasGroundPathNavigation(Mob param0) {
        return param0.getNavigation() instanceof GroundPathNavigation;
    }

    public static boolean mobRestricted(PathfinderMob param0, int param1) {
        return param0.hasRestriction() && param0.getRestrictCenter().closerThan(param0.position(), (double)(param0.getRestrictRadius() + (float)param1) + 1.0);
    }

    public static boolean isOutsideLimits(BlockPos param0, PathfinderMob param1) {
        return param0.getY() < param1.level.getMinBuildHeight() || param0.getY() > param1.level.getMaxBuildHeight();
    }

    public static boolean isRestricted(boolean param0, PathfinderMob param1, BlockPos param2) {
        return param0 && !param1.isWithinRestriction(param2);
    }

    public static boolean isNotStable(PathNavigation param0, BlockPos param1) {
        return !param0.isStableDestination(param1);
    }

    public static boolean isWater(PathfinderMob param0, BlockPos param1) {
        return param0.level.getFluidState(param1).is(FluidTags.WATER);
    }

    public static boolean hasMalus(PathfinderMob param0, BlockPos param1) {
        return param0.getPathfindingMalus(WalkNodeEvaluator.getBlockPathTypeStatic(param0.level, param1.mutable())) != 0.0F;
    }

    public static boolean isSolid(PathfinderMob param0, BlockPos param1) {
        return param0.level.getBlockState(param1).getMaterial().isSolid();
    }
}

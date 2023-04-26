package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

public class RandomStroll {
    private static final int MAX_XZ_DIST = 10;
    private static final int MAX_Y_DIST = 7;
    private static final int[][] SWIM_XY_DISTANCE_TIERS = new int[][]{{1, 1}, {3, 3}, {5, 5}, {6, 5}, {7, 7}, {10, 7}};

    public static OneShot<PathfinderMob> stroll(float param0) {
        return stroll(param0, true);
    }

    public static OneShot<PathfinderMob> stroll(float param0, boolean param1) {
        return strollFlyOrSwim(param0, param0x -> LandRandomPos.getPos(param0x, 10, 7), param1 ? param0x -> true : param0x -> !param0x.isInWaterOrBubble());
    }

    public static BehaviorControl<PathfinderMob> stroll(float param0, int param1, int param2) {
        return strollFlyOrSwim(param0, param2x -> LandRandomPos.getPos(param2x, param1, param2), param0x -> true);
    }

    public static BehaviorControl<PathfinderMob> fly(float param0) {
        return strollFlyOrSwim(param0, param0x -> getTargetFlyPos(param0x, 10, 7), param0x -> true);
    }

    public static BehaviorControl<PathfinderMob> swim(float param0) {
        return strollFlyOrSwim(param0, RandomStroll::getTargetSwimPos, Entity::isInWaterOrBubble);
    }

    private static OneShot<PathfinderMob> strollFlyOrSwim(float param0, Function<PathfinderMob, Vec3> param1, Predicate<PathfinderMob> param2) {
        return BehaviorBuilder.create(
            param3 -> param3.<MemoryAccessor>group(param3.absent(MemoryModuleType.WALK_TARGET)).apply(param3, param3x -> (param4, param5, param6) -> {
                        if (!param2.test(param5)) {
                            return false;
                        } else {
                            Optional<Vec3> var0x = Optional.ofNullable(param1.apply(param5));
                            param3x.setOrErase(var0x.map(param1x -> new WalkTarget(param1x, param0, 0)));
                            return true;
                        }
                    })
        );
    }

    @Nullable
    private static Vec3 getTargetSwimPos(PathfinderMob param0x) {
        Vec3 var0 = null;
        Vec3 var1 = null;

        for(int[] var2 : SWIM_XY_DISTANCE_TIERS) {
            if (var0 == null) {
                var1 = BehaviorUtils.getRandomSwimmablePos(param0x, var2[0], var2[1]);
            } else {
                var1 = param0x.position().add(param0x.position().vectorTo(var0).normalize().multiply((double)var2[0], (double)var2[1], (double)var2[0]));
            }

            if (var1 == null || param0x.level().getFluidState(BlockPos.containing(var1)).isEmpty()) {
                return var0;
            }

            var0 = var1;
        }

        return var1;
    }

    @Nullable
    private static Vec3 getTargetFlyPos(PathfinderMob param0, int param1, int param2) {
        Vec3 var0 = param0.getViewVector(0.0F);
        return AirAndWaterRandomPos.getPos(param0, param1, param2, -2, var0.x, var0.z, (float) (Math.PI / 2));
    }
}

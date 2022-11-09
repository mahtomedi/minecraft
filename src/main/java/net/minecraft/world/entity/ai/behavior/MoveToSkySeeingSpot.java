package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public class MoveToSkySeeingSpot {
    public static OneShot<LivingEntity> create(float param0) {
        return BehaviorBuilder.create(
            param1 -> param1.<MemoryAccessor>group(param1.absent(MemoryModuleType.WALK_TARGET)).apply(param1, param1x -> (param2, param3, param4) -> {
                        if (param2.canSeeSky(param3.blockPosition())) {
                            return false;
                        } else {
                            Optional<Vec3> var0x = Optional.ofNullable(getOutdoorPosition(param2, param3));
                            var0x.ifPresent(param2x -> param1x.set(new WalkTarget(param2x, param0, 0)));
                            return true;
                        }
                    })
        );
    }

    @Nullable
    private static Vec3 getOutdoorPosition(ServerLevel param0, LivingEntity param1) {
        RandomSource var0 = param1.getRandom();
        BlockPos var1 = param1.blockPosition();

        for(int var2 = 0; var2 < 10; ++var2) {
            BlockPos var3 = var1.offset(var0.nextInt(20) - 10, var0.nextInt(6) - 3, var0.nextInt(20) - 10);
            if (hasNoBlocksAbove(param0, param1, var3)) {
                return Vec3.atBottomCenterOf(var3);
            }
        }

        return null;
    }

    public static boolean hasNoBlocksAbove(ServerLevel param0, LivingEntity param1, BlockPos param2) {
        return param0.canSeeSky(param2) && (double)param0.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, param2).getY() <= param1.getY();
    }
}

package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class GoToCelebrateLocation<E extends Mob> extends Behavior<E> {
    private final int closeEnoughDist;

    public GoToCelebrateLocation(int param0) {
        super(
            ImmutableMap.of(
                MemoryModuleType.CELEBRATE_LOCATION,
                MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.ATTACK_TARGET,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.WALK_TARGET,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.LOOK_TARGET,
                MemoryStatus.REGISTERED
            )
        );
        this.closeEnoughDist = param0;
    }

    protected void start(ServerLevel param0, Mob param1, long param2) {
        BlockPos var0 = getCelebrateLocation(param1);
        boolean var1 = var0.closerThan(param1.getBlockPos(), (double)this.closeEnoughDist);
        if (!var1) {
            BehaviorUtils.setWalkAndLookTargetMemories(param1, getNearbyPos(param1, var0), this.closeEnoughDist);
        }

    }

    private static BlockPos getNearbyPos(Mob param0, BlockPos param1) {
        Random var0 = param0.level.random;
        return param1.offset(getRandomOffset(var0), 0, getRandomOffset(var0));
    }

    private static int getRandomOffset(Random param0) {
        return param0.nextInt(3) - 1;
    }

    private static BlockPos getCelebrateLocation(Mob param0) {
        return param0.getBrain().getMemory(MemoryModuleType.CELEBRATE_LOCATION).get();
    }
}

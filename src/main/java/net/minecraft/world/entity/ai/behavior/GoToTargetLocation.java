package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class GoToTargetLocation<E extends Mob> extends Behavior<E> {
    private final MemoryModuleType<BlockPos> locationMemory;
    private final int closeEnoughDist;
    private final float speedModifier;

    public GoToTargetLocation(MemoryModuleType<BlockPos> param0, int param1, float param2) {
        super(
            ImmutableMap.of(
                param0,
                MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.ATTACK_TARGET,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.WALK_TARGET,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.LOOK_TARGET,
                MemoryStatus.REGISTERED
            )
        );
        this.locationMemory = param0;
        this.closeEnoughDist = param1;
        this.speedModifier = param2;
    }

    protected void start(ServerLevel param0, Mob param1, long param2) {
        BlockPos var0 = this.getTargetLocation(param1);
        boolean var1 = var0.closerThan(param1.blockPosition(), (double)this.closeEnoughDist);
        if (!var1) {
            BehaviorUtils.setWalkAndLookTargetMemories(param1, getNearbyPos(param1, var0), this.speedModifier, this.closeEnoughDist);
        }

    }

    private static BlockPos getNearbyPos(Mob param0, BlockPos param1) {
        RandomSource var0 = param0.level.random;
        return param1.offset(getRandomOffset(var0), 0, getRandomOffset(var0));
    }

    private static int getRandomOffset(RandomSource param0) {
        return param0.nextInt(3) - 1;
    }

    private BlockPos getTargetLocation(Mob param0) {
        return param0.getBrain().getMemory(this.locationMemory).get();
    }
}

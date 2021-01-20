package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class TryFindWater extends Behavior<PathfinderMob> {
    private final int range;
    private final float speedModifier;

    public TryFindWater(int param0, float param1) {
        super(
            ImmutableMap.of(
                MemoryModuleType.ATTACK_TARGET,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.WALK_TARGET,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.LOOK_TARGET,
                MemoryStatus.REGISTERED
            )
        );
        this.range = param0;
        this.speedModifier = param1;
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, PathfinderMob param1) {
        return !param1.level.getFluidState(param1.blockPosition()).is(FluidTags.WATER);
    }

    protected void start(ServerLevel param0, PathfinderMob param1, long param2) {
        BlockPos var0 = null;

        for(BlockPos var2 : BlockPos.withinManhattan(param1.blockPosition(), this.range, this.range, this.range)) {
            if (param1.level.getFluidState(var2).is(FluidTags.WATER)) {
                var0 = var2.immutable();
                break;
            }
        }

        if (var0 != null) {
            BehaviorUtils.setWalkAndLookTargetMemories(param1, var0, this.speedModifier, 0);
        }

    }
}

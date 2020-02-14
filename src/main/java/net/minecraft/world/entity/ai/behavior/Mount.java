package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class Mount<E extends LivingEntity> extends Behavior<E> {
    public Mount() {
        super(
            ImmutableMap.of(
                MemoryModuleType.LOOK_TARGET,
                MemoryStatus.REGISTERED,
                MemoryModuleType.WALK_TARGET,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.RIDE_TARGET,
                MemoryStatus.VALUE_PRESENT
            )
        );
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel param0, E param1) {
        return !param1.isPassenger();
    }

    @Override
    protected void start(ServerLevel param0, E param1, long param2) {
        if (this.isCloseEnoughToStartRiding(param1)) {
            param1.startRiding(this.getRidableEntity(param1));
        } else {
            BehaviorUtils.setWalkAndLookTargetMemories(param1, this.getRidableEntity(param1), 1);
        }

    }

    private boolean isCloseEnoughToStartRiding(E param0) {
        return this.getRidableEntity(param0).closerThan(param0, 1.0);
    }

    private Entity getRidableEntity(E param0) {
        return param0.getBrain().getMemory(MemoryModuleType.RIDE_TARGET).get();
    }
}

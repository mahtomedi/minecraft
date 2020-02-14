package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class StopBeingAngryIfTargetDead<E extends Mob> extends Behavior<E> {
    public StopBeingAngryIfTargetDead() {
        super(ImmutableMap.of(MemoryModuleType.ANGRY_AT, MemoryStatus.VALUE_PRESENT));
    }

    protected void start(ServerLevel param0, E param1, long param2) {
        if (this.isCurrentTargetDeadOrRemoved(param1)) {
            param1.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
        }

    }

    private boolean isCurrentTargetDeadOrRemoved(E param0) {
        Optional<LivingEntity> var0 = BehaviorUtils.getLivingEntityFromUUIDMemory(param0, MemoryModuleType.ANGRY_AT);
        return !var0.isPresent() || !var0.get().isAlive();
    }
}

package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.GameRules;

public class StopBeingAngryIfTargetDead<E extends Mob> extends Behavior<E> {
    public StopBeingAngryIfTargetDead() {
        super(ImmutableMap.of(MemoryModuleType.ANGRY_AT, MemoryStatus.VALUE_PRESENT));
    }

    protected void start(ServerLevel param0, E param1, long param2) {
        BehaviorUtils.getLivingEntityFromUUIDMemory(param1, MemoryModuleType.ANGRY_AT).ifPresent(param2x -> {
            if (param2x.isDeadOrDying() && (param2x.getType() != EntityType.PLAYER || param0.getGameRules().getBoolean(GameRules.RULE_FORGIVE_DEAD_PLAYERS))) {
                param1.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
            }

        });
    }
}

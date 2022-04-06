package net.minecraft.world.entity.monster.warden;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class SetWardenLookTarget extends Behavior<Warden> {
    public SetWardenLookTarget() {
        super(
            ImmutableMap.of(
                MemoryModuleType.DISTURBANCE_LOCATION,
                MemoryStatus.REGISTERED,
                MemoryModuleType.ROAR_TARGET,
                MemoryStatus.REGISTERED,
                MemoryModuleType.ATTACK_TARGET,
                MemoryStatus.VALUE_ABSENT
            )
        );
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, Warden param1) {
        return param1.getBrain().hasMemoryValue(MemoryModuleType.DISTURBANCE_LOCATION) || param1.getBrain().hasMemoryValue(MemoryModuleType.ROAR_TARGET);
    }

    protected void start(ServerLevel param0, Warden param1, long param2) {
        BlockPos var0 = param1.getBrain()
            .getMemory(MemoryModuleType.ROAR_TARGET)
            .map(Entity::blockPosition)
            .or(() -> param1.getBrain().getMemory(MemoryModuleType.DISTURBANCE_LOCATION))
            .get();
        param1.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(var0));
    }
}

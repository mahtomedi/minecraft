package net.minecraft.world.entity.monster.warden;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class SetWardenLookTarget extends Behavior<Warden> {
    public SetWardenLookTarget() {
        super(ImmutableMap.of(MemoryModuleType.DISTURBANCE_LOCATION, MemoryStatus.VALUE_PRESENT, MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT));
    }

    protected void start(ServerLevel param0, Warden param1, long param2) {
        param1.getBrain()
            .setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(param1.getBrain().getMemory(MemoryModuleType.DISTURBANCE_LOCATION).get()));
    }
}

package net.minecraft.world.entity.monster.piglin;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class StopHoldingItemIfNoLongerAdmiring<E extends Piglin> extends Behavior<E> {
    public StopHoldingItemIfNoLongerAdmiring() {
        super(ImmutableMap.of(MemoryModuleType.ADMIRING_ITEM, MemoryStatus.VALUE_ABSENT));
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, E param1) {
        return !param1.getOffhandItem().isEmpty();
    }

    protected void start(ServerLevel param0, E param1, long param2) {
        PiglinAi.stopHoldingOffHandItem(param1, true);
    }
}

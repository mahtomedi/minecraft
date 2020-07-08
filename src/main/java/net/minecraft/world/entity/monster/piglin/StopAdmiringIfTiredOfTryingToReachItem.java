package net.minecraft.world.entity.monster.piglin;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class StopAdmiringIfTiredOfTryingToReachItem<E extends Piglin> extends Behavior<E> {
    private final int maxTimeToReachItem;
    private final int disableTime;

    public StopAdmiringIfTiredOfTryingToReachItem(int param0, int param1) {
        super(
            ImmutableMap.of(
                MemoryModuleType.ADMIRING_ITEM,
                MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM,
                MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM,
                MemoryStatus.REGISTERED,
                MemoryModuleType.DISABLE_WALK_TO_ADMIRE_ITEM,
                MemoryStatus.REGISTERED
            )
        );
        this.maxTimeToReachItem = param0;
        this.disableTime = param1;
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, E param1) {
        return param1.getOffhandItem().isEmpty();
    }

    protected void start(ServerLevel param0, E param1, long param2) {
        Brain<Piglin> var0 = param1.getBrain();
        Optional<Integer> var1 = var0.getMemory(MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM);
        if (!var1.isPresent()) {
            var0.setMemory(MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM, 0);
        } else {
            int var2 = var1.get();
            if (var2 > this.maxTimeToReachItem) {
                var0.eraseMemory(MemoryModuleType.ADMIRING_ITEM);
                var0.eraseMemory(MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM);
                var0.setMemoryWithExpiry(MemoryModuleType.DISABLE_WALK_TO_ADMIRE_ITEM, true, (long)this.disableTime);
            } else {
                var0.setMemory(MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM, var2 + 1);
            }
        }

    }
}

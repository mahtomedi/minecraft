package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.Piglin;

public class StopAdmiringIfItemTooFarAway<E extends Piglin> extends Behavior<E> {
    private final int maxDistanceToItem;

    public StopAdmiringIfItemTooFarAway(int param0) {
        super(
            ImmutableMap.of(MemoryModuleType.ADMIRING_ITEM, MemoryStatus.VALUE_PRESENT, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryStatus.REGISTERED)
        );
        this.maxDistanceToItem = param0;
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, E param1) {
        if (!param1.getOffhandItem().isEmpty()) {
            return false;
        } else {
            Optional<ItemEntity> var0 = param1.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM);
            if (!var0.isPresent()) {
                return true;
            } else {
                return !var0.get().closerThan(param1, (double)this.maxDistanceToItem);
            }
        }
    }

    protected void start(ServerLevel param0, E param1, long param2) {
        param1.getBrain().eraseMemory(MemoryModuleType.ADMIRING_ITEM);
    }
}

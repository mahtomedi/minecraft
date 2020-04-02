package net.minecraft.world.entity.monster.piglin;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.SerializableBoolean;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.item.ItemEntity;

public class StartAdmiringItemIfSeen<E extends Piglin> extends Behavior<E> {
    private final int admireDuration;

    public StartAdmiringItemIfSeen(int param0) {
        super(
            ImmutableMap.of(
                MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM,
                MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.ADMIRING_ITEM,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.ADMIRING_DISABLED,
                MemoryStatus.VALUE_ABSENT
            )
        );
        this.admireDuration = param0;
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, E param1) {
        ItemEntity var0 = param1.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM).get();
        return PiglinAi.isLovedItem(var0.getItem().getItem());
    }

    protected void start(ServerLevel param0, E param1, long param2) {
        param1.getBrain().setMemoryWithExpiry(MemoryModuleType.ADMIRING_ITEM, SerializableBoolean.of(true), (long)this.admireDuration);
    }
}
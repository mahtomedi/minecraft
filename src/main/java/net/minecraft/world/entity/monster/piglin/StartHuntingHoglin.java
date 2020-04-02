package net.minecraft.world.entity.monster.piglin;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.hoglin.Hoglin;

public class StartHuntingHoglin<E extends Piglin> extends Behavior<E> {
    public StartHuntingHoglin() {
        super(
            ImmutableMap.of(
                MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN,
                MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.ANGRY_AT,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.HUNTED_RECENTLY,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS,
                MemoryStatus.REGISTERED
            )
        );
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, Piglin param1) {
        return !param1.isBaby() && !PiglinAi.hasAnyoneNearbyHuntedRecently(param1);
    }

    protected void start(ServerLevel param0, E param1, long param2) {
        Hoglin var0 = param1.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN).get();
        PiglinAi.setAngerTarget(param1, var0);
        PiglinAi.dontKillAnyMoreHoglinsForAWhile(param1);
        PiglinAi.broadcastAngerTarget(param1, var0);
        PiglinAi.broadcastDontKillAnyMoreHoglinsForAWhile(param1);
    }
}

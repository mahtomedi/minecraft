package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;

public class VillagerCalmDown extends Behavior<Villager> {
    public VillagerCalmDown() {
        super(ImmutableMap.of());
    }

    protected void start(ServerLevel param0, Villager param1, long param2) {
        boolean var0 = VillagerPanicTrigger.isHurt(param1) || VillagerPanicTrigger.hasHostile(param1) || isCloseToEntityThatHurtMe(param1);
        if (!var0) {
            param1.getBrain().eraseMemory(MemoryModuleType.HURT_BY);
            param1.getBrain().eraseMemory(MemoryModuleType.HURT_BY_ENTITY);
            param1.getBrain().updateActivityFromSchedule(param0.getDayTime(), param0.getGameTime());
        }

    }

    private static boolean isCloseToEntityThatHurtMe(Villager param0) {
        return param0.getBrain().getMemory(MemoryModuleType.HURT_BY_ENTITY).filter(param1 -> param1.distanceToSqr(param0) <= 36.0).isPresent();
    }
}

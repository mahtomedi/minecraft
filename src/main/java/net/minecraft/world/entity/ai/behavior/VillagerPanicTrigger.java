package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Activity;

public class VillagerPanicTrigger extends Behavior<Villager> {
    public VillagerPanicTrigger() {
        super(ImmutableMap.of());
    }

    protected boolean canStillUse(ServerLevel param0, Villager param1, long param2) {
        return isHurt(param1) || hasHostile(param1);
    }

    protected void start(ServerLevel param0, Villager param1, long param2) {
        if (isHurt(param1) || hasHostile(param1)) {
            Brain<?> var0 = param1.getBrain();
            if (!var0.isActive(Activity.PANIC)) {
                var0.eraseMemory(MemoryModuleType.PATH);
                var0.eraseMemory(MemoryModuleType.WALK_TARGET);
                var0.eraseMemory(MemoryModuleType.LOOK_TARGET);
                var0.eraseMemory(MemoryModuleType.BREED_TARGET);
                var0.eraseMemory(MemoryModuleType.INTERACTION_TARGET);
            }

            var0.setActiveActivityIfPossible(Activity.PANIC);
        }

    }

    protected void tick(ServerLevel param0, Villager param1, long param2) {
        if (param2 % 100L == 0L) {
            param1.spawnGolemIfNeeded(param2, 3);
        }

    }

    public static boolean hasHostile(LivingEntity param0) {
        return param0.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_HOSTILE);
    }

    public static boolean isHurt(LivingEntity param0) {
        return param0.getBrain().hasMemoryValue(MemoryModuleType.HURT_BY);
    }
}

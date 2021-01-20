package net.minecraft.world.entity.animal.axolotl;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class ValidatePlayDead extends Behavior<Axolotl> {
    public ValidatePlayDead() {
        super(ImmutableMap.of(MemoryModuleType.PLAY_DEAD_TICKS, MemoryStatus.VALUE_PRESENT));
    }

    protected void start(ServerLevel param0, Axolotl param1, long param2) {
        Brain<Axolotl> var0 = param1.getBrain();
        int var1 = var0.getMemory(MemoryModuleType.PLAY_DEAD_TICKS).get();
        if (var1 <= 0) {
            var0.eraseMemory(MemoryModuleType.PLAY_DEAD_TICKS);
            var0.eraseMemory(MemoryModuleType.HURT_BY_ENTITY);
            var0.useDefaultActivity();
        } else {
            var0.setMemory(MemoryModuleType.PLAY_DEAD_TICKS, var1 - 1);
        }

    }
}

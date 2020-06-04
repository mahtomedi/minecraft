package net.minecraft.world.entity.monster.piglin;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class RememberIfHoglinWasKilled<E extends Piglin> extends Behavior<E> {
    public RememberIfHoglinWasKilled() {
        super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT, MemoryModuleType.HUNTED_RECENTLY, MemoryStatus.REGISTERED));
    }

    protected void start(ServerLevel param0, E param1, long param2) {
        if (this.isAttackTargetDeadHoglin(param1)) {
            PiglinAi.dontKillAnyMoreHoglinsForAWhile(param1);
        }

    }

    private boolean isAttackTargetDeadHoglin(E param0) {
        LivingEntity var0 = param0.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
        return var0.getType() == EntityType.HOGLIN && var0.isDeadOrDying();
    }
}

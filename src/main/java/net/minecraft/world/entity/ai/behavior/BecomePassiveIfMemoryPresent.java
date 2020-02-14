package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class BecomePassiveIfMemoryPresent extends Behavior<LivingEntity> {
    private final int pacifyDuration;

    public BecomePassiveIfMemoryPresent(MemoryModuleType<?> param0, int param1) {
        super(
            ImmutableMap.of(
                MemoryModuleType.ATTACK_TARGET,
                MemoryStatus.REGISTERED,
                MemoryModuleType.PACIFIED,
                MemoryStatus.VALUE_ABSENT,
                param0,
                MemoryStatus.VALUE_PRESENT
            )
        );
        this.pacifyDuration = param1;
    }

    @Override
    protected void start(ServerLevel param0, LivingEntity param1, long param2) {
        param1.getBrain().setMemoryWithExpiry(MemoryModuleType.PACIFIED, true, param2, (long)this.pacifyDuration);
        param1.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
    }
}

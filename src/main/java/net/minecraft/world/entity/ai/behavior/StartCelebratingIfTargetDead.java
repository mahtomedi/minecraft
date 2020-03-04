package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class StartCelebratingIfTargetDead extends Behavior<LivingEntity> {
    private final int celebrateDuration;

    public StartCelebratingIfTargetDead(int param0) {
        super(
            ImmutableMap.of(
                MemoryModuleType.ATTACK_TARGET,
                MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.ANGRY_AT,
                MemoryStatus.REGISTERED,
                MemoryModuleType.CELEBRATE_LOCATION,
                MemoryStatus.VALUE_ABSENT
            )
        );
        this.celebrateDuration = param0;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel param0, LivingEntity param1) {
        return this.getAttackTarget(param1).getHealth() <= 0.0F;
    }

    @Override
    protected void start(ServerLevel param0, LivingEntity param1, long param2) {
        BlockPos var0 = this.getAttackTarget(param1).blockPosition();
        param1.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        param1.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
        param1.getBrain().setMemoryWithExpiry(MemoryModuleType.CELEBRATE_LOCATION, var0, (long)this.celebrateDuration);
    }

    private LivingEntity getAttackTarget(LivingEntity param0) {
        return param0.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
    }
}

package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class CountDownTemptationTicks extends Behavior<LivingEntity> {
    public CountDownTemptationTicks() {
        super(ImmutableMap.of(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, MemoryStatus.VALUE_PRESENT));
    }

    private Optional<Integer> getCalmDownTickMemory(LivingEntity param0) {
        return param0.getBrain().getMemory(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS);
    }

    @Override
    protected boolean canStillUse(ServerLevel param0, LivingEntity param1, long param2) {
        Optional<Integer> var0 = this.getCalmDownTickMemory(param1);
        return var0.isPresent() && var0.get() > 0;
    }

    @Override
    protected void tick(ServerLevel param0, LivingEntity param1, long param2) {
        Optional<Integer> var0 = this.getCalmDownTickMemory(param1);
        param1.getBrain().setMemory(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, var0.get() - 1);
    }

    @Override
    protected void stop(ServerLevel param0, LivingEntity param1, long param2) {
        param1.getBrain().eraseMemory(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS);
    }
}

package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class CountDownCooldownTicks extends Behavior<LivingEntity> {
    private final MemoryModuleType<Integer> cooldownTicks;

    public CountDownCooldownTicks(MemoryModuleType<Integer> param0) {
        super(ImmutableMap.of(param0, MemoryStatus.VALUE_PRESENT));
        this.cooldownTicks = param0;
    }

    private Optional<Integer> getCooldownTickMemory(LivingEntity param0) {
        return param0.getBrain().getMemory(this.cooldownTicks);
    }

    @Override
    protected boolean timedOut(long param0) {
        return false;
    }

    @Override
    protected boolean canStillUse(ServerLevel param0, LivingEntity param1, long param2) {
        Optional<Integer> var0 = this.getCooldownTickMemory(param1);
        return var0.isPresent() && var0.get() > 0;
    }

    @Override
    protected void tick(ServerLevel param0, LivingEntity param1, long param2) {
        Optional<Integer> var0 = this.getCooldownTickMemory(param1);
        param1.getBrain().setMemory(this.cooldownTicks, var0.get() - 1);
    }

    @Override
    protected void stop(ServerLevel param0, LivingEntity param1, long param2) {
        param1.getBrain().eraseMemory(this.cooldownTicks);
    }
}

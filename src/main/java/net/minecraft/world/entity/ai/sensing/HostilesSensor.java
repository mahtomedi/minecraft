package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public abstract class HostilesSensor extends Sensor<LivingEntity> {
    protected abstract Optional<LivingEntity> getNearestHostile(LivingEntity var1);

    protected abstract boolean isClose(LivingEntity var1, LivingEntity var2);

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.NEAREST_HOSTILE);
    }

    @Override
    protected void doTick(ServerLevel param0, LivingEntity param1) {
        param1.getBrain().setMemory(MemoryModuleType.NEAREST_HOSTILE, this.getNearestHostile(param1));
    }

    protected Optional<List<LivingEntity>> getVisibleEntities(LivingEntity param0) {
        return param0.getBrain().getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES);
    }
}

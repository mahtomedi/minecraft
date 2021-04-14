package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public abstract class NearestVisibleLivingEntitySensor extends Sensor<LivingEntity> {
    protected abstract boolean isMatchingEntity(LivingEntity var1, LivingEntity var2);

    protected abstract MemoryModuleType<LivingEntity> getMemory();

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(this.getMemory());
    }

    @Override
    protected void doTick(ServerLevel param0, LivingEntity param1) {
        param1.getBrain().setMemory(this.getMemory(), this.getNearestEntity(param1));
    }

    private Optional<LivingEntity> getNearestEntity(LivingEntity param0) {
        return this.getVisibleEntities(param0)
            .flatMap(param1 -> param1.stream().filter(param1x -> this.isMatchingEntity(param0, param1x)).min(Comparator.comparingDouble(param0::distanceToSqr)));
    }

    protected Optional<List<LivingEntity>> getVisibleEntities(LivingEntity param0) {
        return param0.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
    }
}

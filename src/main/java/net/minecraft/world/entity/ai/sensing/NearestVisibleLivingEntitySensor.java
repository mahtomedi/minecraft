package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

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
            .flatMap(param1 -> param1.findClosest((Predicate<LivingEntity>)(param1x -> this.isMatchingEntity(param0, param1x))));
    }

    protected Optional<NearestVisibleLivingEntities> getVisibleEntities(LivingEntity param0) {
        return param0.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
    }
}

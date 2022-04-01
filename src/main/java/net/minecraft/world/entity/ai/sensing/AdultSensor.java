package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

public class AdultSensor extends Sensor<AgeableMob> {
    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.NEAREST_VISIBLE_ADULT, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
    }

    protected void doTick(ServerLevel param0, AgeableMob param1) {
        param1.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).ifPresent(param1x -> this.setNearestVisibleAdult(param1, param1x));
    }

    private void setNearestVisibleAdult(AgeableMob param0, NearestVisibleLivingEntities param1) {
        Optional<AgeableMob> var0 = param1.findClosest(param1x -> param1x.getType() == param0.getType() && !param1x.isBaby()).map(AgeableMob.class::cast);
        param0.getBrain().setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT, var0);
    }
}

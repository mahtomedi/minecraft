package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class AdultSensor extends Sensor<AgeableMob> {
    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.NEAREST_VISIBLE_ADULT, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
    }

    protected void doTick(ServerLevel param0, AgeableMob param1) {
        param1.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).ifPresent(param1x -> this.setNearestVisibleAdult(param1, param1x));
    }

    private void setNearestVisibleAdult(AgeableMob param0, List<LivingEntity> param1) {
        Optional<AgeableMob> var0 = param1.stream()
            .filter(param1x -> param1x.getType() == param0.getType())
            .map(param0x -> (AgeableMob)param0x)
            .filter(param0x -> !param0x.isBaby())
            .findFirst();
        param0.getBrain().setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT, var0);
    }
}

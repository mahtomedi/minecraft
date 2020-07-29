package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.phys.AABB;

public class NearestLivingEntitySensor extends Sensor<LivingEntity> {
    @Override
    protected void doTick(ServerLevel param0, LivingEntity param1) {
        AABB var0 = param1.getBoundingBox().inflate(16.0, 16.0, 16.0);
        List<LivingEntity> var1 = param0.getEntitiesOfClass(LivingEntity.class, var0, param1x -> param1x != param1 && param1x.isAlive());
        var1.sort(Comparator.comparingDouble(param1::distanceToSqr));
        Brain<?> var2 = param1.getBrain();
        var2.setMemory(MemoryModuleType.LIVING_ENTITIES, var1);
        var2.setMemory(
            MemoryModuleType.VISIBLE_LIVING_ENTITIES, var1.stream().filter(param1x -> isEntityTargetable(param1, param1x)).collect(Collectors.toList())
        );
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.LIVING_ENTITIES, MemoryModuleType.VISIBLE_LIVING_ENTITIES);
    }
}

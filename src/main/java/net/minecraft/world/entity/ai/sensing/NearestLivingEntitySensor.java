package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.phys.AABB;

public class NearestLivingEntitySensor extends Sensor<LivingEntity> {
    @Override
    protected void doTick(ServerLevel param0, LivingEntity param1) {
        AABB var0 = param1.getBoundingBox().inflate(16.0, 16.0, 16.0);
        List<LivingEntity> var1 = param0.getEntitiesOfClass(LivingEntity.class, var0, param1x -> param1x != param1 && param1x.isAlive());
        var1.sort(Comparator.comparingDouble(param1::distanceToSqr));
        Brain<?> var2 = param1.getBrain();
        var2.setMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES, var1);
        var2.setMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, new NearestVisibleLivingEntities(param1, var1));
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
    }
}

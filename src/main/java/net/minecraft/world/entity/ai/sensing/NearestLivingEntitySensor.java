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
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public class NearestLivingEntitySensor extends Sensor<LivingEntity> {
    private static final TargetingConditions TARGETING = new TargetingConditions().range(16.0).allowSameTeam().allowNonAttackable().allowUnseeable();

    @Override
    protected void doTick(ServerLevel param0, LivingEntity param1) {
        List<LivingEntity> var0 = param0.getEntitiesOfClass(
            LivingEntity.class, param1.getBoundingBox().inflate(16.0, 16.0, 16.0), param1x -> param1x != param1 && param1x.isAlive()
        );
        var0.sort(Comparator.comparingDouble(param1::distanceToSqr));
        Brain<?> var1 = param1.getBrain();
        var1.setMemory(MemoryModuleType.LIVING_ENTITIES, var0);
        var1.setMemory(
            MemoryModuleType.VISIBLE_LIVING_ENTITIES,
            var0.stream().filter(param1x -> TARGETING.test(param1, param1x)).filter(param1::canSee).collect(Collectors.toList())
        );
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.LIVING_ENTITIES, MemoryModuleType.VISIBLE_LIVING_ENTITIES);
    }
}

package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.warden.Warden;

public class WardenEntitySensor extends Sensor<LivingEntity> {
    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.NEAREST_ATTACKABLE);
    }

    @Override
    protected void doTick(ServerLevel param0, LivingEntity param1) {
        param1.getBrain()
            .getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
            .flatMap(
                param0x -> param0x.findClosest(
                        param0xx -> Warden.canTargetEntity(param0xx) && param0xx.getType() == EntityType.PLAYER,
                        param0xx -> Warden.canTargetEntity(param0xx) && param0xx.getType() != EntityType.PLAYER
                    )
            )
            .ifPresent(param1x -> param1.getBrain().setMemory(MemoryModuleType.NEAREST_ATTACKABLE, param1x));
    }
}

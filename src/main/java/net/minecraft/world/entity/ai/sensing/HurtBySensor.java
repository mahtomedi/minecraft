package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class HurtBySensor extends Sensor<LivingEntity> {
    @Override
    protected void doTick(ServerLevel param0, LivingEntity param1) {
        Brain<?> var0 = param1.getBrain();
        if (param1.getLastDamageSource() != null) {
            var0.setMemory(MemoryModuleType.HURT_BY, param1.getLastDamageSource());
            Entity var1 = var0.getMemory(MemoryModuleType.HURT_BY).get().getEntity();
            if (var1 instanceof LivingEntity) {
                var0.setMemory(MemoryModuleType.HURT_BY_ENTITY, (LivingEntity)var1);
            }
        } else {
            var0.eraseMemory(MemoryModuleType.HURT_BY);
        }

    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY);
    }
}

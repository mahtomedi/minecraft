package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class HurtBySensor extends Sensor<LivingEntity> {
    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY);
    }

    @Override
    protected void doTick(ServerLevel param0, LivingEntity param1) {
        Brain<?> var0 = param1.getBrain();
        DamageSource var1 = param1.getLastDamageSource();
        if (var1 != null) {
            var0.setMemory(MemoryModuleType.HURT_BY, param1.getLastDamageSource());
            Entity var2 = var1.getEntity();
            if (var2 instanceof LivingEntity) {
                var0.setMemory(MemoryModuleType.HURT_BY_ENTITY, (LivingEntity)var2);
            }
        } else {
            var0.eraseMemory(MemoryModuleType.HURT_BY);
        }

        var0.getMemory(MemoryModuleType.HURT_BY_ENTITY).ifPresent(param2 -> {
            if (!param2.isAlive() || param2.level() != param0) {
                var0.eraseMemory(MemoryModuleType.HURT_BY_ENTITY);
            }

        });
    }
}

package net.minecraft.world.entity.ai.sensing;

import java.util.Comparator;
import java.util.Optional;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class AxolotlHostileSensor extends HostilesSensor {
    @Override
    protected Optional<LivingEntity> getNearestHostile(LivingEntity param0) {
        return this.getVisibleEntities(param0)
            .flatMap(
                param1 -> param1.stream()
                        .filter(param1x -> this.shouldTarget(param0, param1x))
                        .filter(param1x -> this.isClose(param0, param1x))
                        .filter(Entity::isInWaterOrBubble)
                        .min(Comparator.comparingDouble(param0::distanceToSqr))
            );
    }

    private boolean shouldTarget(LivingEntity param0, LivingEntity param1) {
        EntityType<?> var0 = param1.getType();
        if (EntityTypeTags.AXOLOTL_ALWAYS_HOSTILES.contains(var0)) {
            return true;
        } else if (!EntityTypeTags.AXOLOTL_TEMPTED_HOSTILES.contains(var0)) {
            return false;
        } else {
            Optional<Boolean> var1 = param0.getBrain().getMemory(MemoryModuleType.IS_TEMPTED);
            return var1.isPresent() && var1.get();
        }
    }

    @Override
    protected boolean isClose(LivingEntity param0, LivingEntity param1) {
        return param1.distanceToSqr(param0) <= 64.0;
    }
}

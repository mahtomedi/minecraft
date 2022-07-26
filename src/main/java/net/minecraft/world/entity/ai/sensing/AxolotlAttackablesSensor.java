package net.minecraft.world.entity.ai.sensing;

import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class AxolotlAttackablesSensor extends NearestVisibleLivingEntitySensor {
    public static final float TARGET_DETECTION_DISTANCE = 8.0F;

    @Override
    protected boolean isMatchingEntity(LivingEntity param0, LivingEntity param1) {
        return this.isClose(param0, param1)
            && param1.isInWaterOrBubble()
            && (this.isHostileTarget(param1) || this.isHuntTarget(param0, param1))
            && Sensor.isEntityAttackable(param0, param1);
    }

    private boolean isHuntTarget(LivingEntity param0, LivingEntity param1) {
        return !param0.getBrain().hasMemoryValue(MemoryModuleType.HAS_HUNTING_COOLDOWN) && param1.getType().is(EntityTypeTags.AXOLOTL_HUNT_TARGETS);
    }

    private boolean isHostileTarget(LivingEntity param0) {
        return param0.getType().is(EntityTypeTags.AXOLOTL_ALWAYS_HOSTILES);
    }

    private boolean isClose(LivingEntity param0, LivingEntity param1) {
        return param1.distanceToSqr(param0) <= 64.0;
    }

    @Override
    protected MemoryModuleType<LivingEntity> getMemory() {
        return MemoryModuleType.NEAREST_ATTACKABLE;
    }
}

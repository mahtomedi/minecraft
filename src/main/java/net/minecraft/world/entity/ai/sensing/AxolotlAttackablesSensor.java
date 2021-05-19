package net.minecraft.world.entity.ai.sensing;

import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class AxolotlAttackablesSensor extends NearestVisibleLivingEntitySensor {
    public static final float TARGET_DETECTION_DISTANCE = 8.0F;

    @Override
    protected boolean isMatchingEntity(LivingEntity param0, LivingEntity param1) {
        if (Sensor.isEntityAttackable(param0, param1) && (this.isHostileTarget(param1) || this.isHuntTarget(param0, param1))) {
            return this.isClose(param0, param1) && param1.isInWaterOrBubble();
        } else {
            return false;
        }
    }

    private boolean isHuntTarget(LivingEntity param0, LivingEntity param1) {
        return !param0.getBrain().hasMemoryValue(MemoryModuleType.HAS_HUNTING_COOLDOWN) && EntityTypeTags.AXOLOTL_HUNT_TARGETS.contains(param1.getType());
    }

    private boolean isHostileTarget(LivingEntity param0) {
        return EntityTypeTags.AXOLOTL_ALWAYS_HOSTILES.contains(param0.getType());
    }

    private boolean isClose(LivingEntity param0, LivingEntity param1) {
        return param1.distanceToSqr(param0) <= 64.0;
    }

    @Override
    protected MemoryModuleType<LivingEntity> getMemory() {
        return MemoryModuleType.NEAREST_ATTACKABLE;
    }
}

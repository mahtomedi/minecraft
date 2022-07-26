package net.minecraft.world.entity.ai.sensing;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.frog.Frog;

public class FrogAttackablesSensor extends NearestVisibleLivingEntitySensor {
    public static final float TARGET_DETECTION_DISTANCE = 10.0F;

    @Override
    protected boolean isMatchingEntity(LivingEntity param0, LivingEntity param1) {
        return !param0.getBrain().hasMemoryValue(MemoryModuleType.HAS_HUNTING_COOLDOWN)
                && Sensor.isEntityAttackable(param0, param1)
                && Frog.canEat(param1)
                && !this.isUnreachableAttackTarget(param0, param1)
            ? param1.closerThan(param0, 10.0)
            : false;
    }

    private boolean isUnreachableAttackTarget(LivingEntity param0, LivingEntity param1) {
        List<UUID> var0 = param0.getBrain().getMemory(MemoryModuleType.UNREACHABLE_TONGUE_TARGETS).orElseGet(ArrayList::new);
        return var0.contains(param1.getUUID());
    }

    @Override
    protected MemoryModuleType<LivingEntity> getMemory() {
        return MemoryModuleType.NEAREST_ATTACKABLE;
    }
}

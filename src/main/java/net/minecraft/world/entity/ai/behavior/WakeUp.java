package net.minecraft.world.entity.ai.behavior;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.schedule.Activity;

public class WakeUp {
    public static BehaviorControl<LivingEntity> create() {
        return BehaviorBuilder.create(param0 -> param0.point((param0x, param1, param2) -> {
                if (!param1.getBrain().isActive(Activity.REST) && param1.isSleeping()) {
                    param1.stopSleeping();
                    return true;
                } else {
                    return false;
                }
            }));
    }
}

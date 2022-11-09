package net.minecraft.world.entity.ai.behavior;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;

public class UpdateActivityFromSchedule {
    public static BehaviorControl<LivingEntity> create() {
        return BehaviorBuilder.create(param0 -> param0.point((param0x, param1, param2) -> {
                param1.getBrain().updateActivityFromSchedule(param0x.getDayTime(), param0x.getGameTime());
                return true;
            }));
    }
}

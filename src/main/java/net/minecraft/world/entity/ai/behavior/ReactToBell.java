package net.minecraft.world.entity.ai.behavior;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.schedule.Activity;

public class ReactToBell {
    public static BehaviorControl<LivingEntity> create() {
        return BehaviorBuilder.create(
            param0 -> param0.<MemoryAccessor>group(param0.present(MemoryModuleType.HEARD_BELL_TIME)).apply(param0, param0x -> (param0xx, param1, param2) -> {
                        Raid var0x = param0xx.getRaidAt(param1.blockPosition());
                        if (var0x == null) {
                            param1.getBrain().setActiveActivityIfPossible(Activity.HIDE);
                        }
    
                        return true;
                    })
        );
    }
}

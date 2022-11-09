package net.minecraft.world.entity.animal.axolotl;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class ValidatePlayDead {
    public static BehaviorControl<LivingEntity> create() {
        return BehaviorBuilder.create(
            param0 -> param0.<MemoryAccessor, MemoryAccessor>group(
                        param0.present(MemoryModuleType.PLAY_DEAD_TICKS), param0.registered(MemoryModuleType.HURT_BY_ENTITY)
                    )
                    .apply(param0, (param1, param2) -> (param3, param4, param5) -> {
                            int var0x = param0.<Integer>get(param1);
                            if (var0x <= 0) {
                                param1.erase();
                                param2.erase();
                                param4.getBrain().useDefaultActivity();
                            } else {
                                param1.set(var0x - 1);
                            }
        
                            return true;
                        })
        );
    }
}

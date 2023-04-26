package net.minecraft.world.entity.monster.piglin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class RememberIfHoglinWasKilled {
    public static BehaviorControl<LivingEntity> create() {
        return BehaviorBuilder.create(
            param0 -> param0.<MemoryAccessor, MemoryAccessor>group(
                        param0.present(MemoryModuleType.ATTACK_TARGET), param0.registered(MemoryModuleType.HUNTED_RECENTLY)
                    )
                    .apply(param0, (param1, param2) -> (param3, param4, param5) -> {
                            LivingEntity var0x = param0.get(param1);
                            if (var0x.getType() == EntityType.HOGLIN && var0x.isDeadOrDying()) {
                                param2.setWithExpiry(true, (long)PiglinAi.TIME_BETWEEN_HUNTS.sample(param4.level().random));
                            }
        
                            return true;
                        })
        );
    }
}

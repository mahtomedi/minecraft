package net.minecraft.world.entity.ai.behavior;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class VillagerCalmDown {
    private static final int SAFE_DISTANCE_FROM_DANGER = 36;

    public static BehaviorControl<LivingEntity> create() {
        return BehaviorBuilder.create(
            param0 -> param0.<MemoryAccessor, MemoryAccessor, MemoryAccessor>group(
                        param0.registered(MemoryModuleType.HURT_BY),
                        param0.registered(MemoryModuleType.HURT_BY_ENTITY),
                        param0.registered(MemoryModuleType.NEAREST_HOSTILE)
                    )
                    .apply(
                        param0,
                        (param1, param2, param3) -> (param4, param5, param6) -> {
                                boolean var0x = param0.tryGet(param1).isPresent()
                                    || param0.tryGet(param3).isPresent()
                                    || param0.<LivingEntity>tryGet(param2).filter(param1x -> param1x.distanceToSqr(param5) <= 36.0).isPresent();
                                if (!var0x) {
                                    param1.erase();
                                    param2.erase();
                                    param5.getBrain().updateActivityFromSchedule(param4.getDayTime(), param4.getGameTime());
                                }
            
                                return true;
                            }
                    )
        );
    }
}

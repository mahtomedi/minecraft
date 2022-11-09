package net.minecraft.world.entity.monster.piglin;

import java.util.Optional;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class StopAdmiringIfTiredOfTryingToReachItem {
    public static BehaviorControl<LivingEntity> create(int param0, int param1) {
        return BehaviorBuilder.create(
            param2 -> param2.<MemoryAccessor, MemoryAccessor, MemoryAccessor, MemoryAccessor>group(
                        param2.present(MemoryModuleType.ADMIRING_ITEM),
                        param2.present(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM),
                        param2.registered(MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM),
                        param2.registered(MemoryModuleType.DISABLE_WALK_TO_ADMIRE_ITEM)
                    )
                    .apply(param2, (param3, param4, param5, param6) -> (param6x, param7, param8) -> {
                            if (!param7.getOffhandItem().isEmpty()) {
                                return false;
                            } else {
                                Optional<Integer> var0x = param2.tryGet(param5);
                                if (var0x.isEmpty()) {
                                    param5.set(0);
                                } else {
                                    int var1x = var0x.get();
                                    if (var1x > param0) {
                                        param3.erase();
                                        param5.erase();
                                        param6.setWithExpiry(true, (long)param1);
                                    } else {
                                        param5.set(var1x + 1);
                                    }
                                }
        
                                return true;
                            }
                        })
        );
    }
}

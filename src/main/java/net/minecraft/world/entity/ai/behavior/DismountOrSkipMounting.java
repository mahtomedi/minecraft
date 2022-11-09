package net.minecraft.world.entity.ai.behavior;

import java.util.function.BiPredicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class DismountOrSkipMounting {
    public static <E extends LivingEntity> BehaviorControl<E> create(int param0, BiPredicate<E, Entity> param1) {
        return BehaviorBuilder.create(
            param2 -> param2.<MemoryAccessor>group(param2.registered(MemoryModuleType.RIDE_TARGET)).apply(param2, param3 -> (param4, param5, param6) -> {
                        Entity var0x = param5.getVehicle();
                        Entity var1x = param2.<Entity>tryGet(param3).orElse(null);
                        if (var0x == null && var1x == null) {
                            return false;
                        } else {
                            Entity var2x = var0x == null ? var1x : var0x;
                            if (isVehicleValid(param5, var2x, param0) && !param1.test(param5, var2x)) {
                                return false;
                            } else {
                                param5.stopRiding();
                                param3.erase();
                                return true;
                            }
                        }
                    })
        );
    }

    private static boolean isVehicleValid(LivingEntity param0, Entity param1, int param2) {
        return param1.isAlive() && param1.closerThan(param0, (double)param2) && param1.level == param0.level;
    }
}

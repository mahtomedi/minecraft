package net.minecraft.world.entity.ai.behavior;

import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class EraseMemoryIf {
    public static <E extends LivingEntity> BehaviorControl<E> create(Predicate<E> param0, MemoryModuleType<?> param1) {
        return BehaviorBuilder.create(param2 -> param2.<MemoryAccessor>group(param2.present(param1)).apply(param2, param1x -> (param2x, param3, param4) -> {
                    if (param0.test(param3)) {
                        param1x.erase();
                        return true;
                    } else {
                        return false;
                    }
                }));
    }
}

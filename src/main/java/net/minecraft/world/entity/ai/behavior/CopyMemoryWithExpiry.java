package net.minecraft.world.entity.ai.behavior;

import java.util.function.Predicate;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class CopyMemoryWithExpiry {
    public static <E extends LivingEntity, T> BehaviorControl<E> create(
        Predicate<E> param0, MemoryModuleType<? extends T> param1, MemoryModuleType<T> param2, UniformInt param3
    ) {
        return BehaviorBuilder.create(
            param4 -> param4.<MemoryAccessor, MemoryAccessor>group(param4.present(param1), param4.absent(param2))
                    .apply(param4, (param3x, param4x) -> (param5, param6, param7) -> {
                            if (!param0.test(param6)) {
                                return false;
                            } else {
                                param4x.setWithExpiry(param4.get(param3x), (long)param3.sample(param5.random));
                                return true;
                            }
                        })
        );
    }
}

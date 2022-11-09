package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class StartAttacking {
    public static <E extends Mob> BehaviorControl<E> create(Function<E, Optional<? extends LivingEntity>> param0) {
        return create(param0x -> true, param0);
    }

    public static <E extends Mob> BehaviorControl<E> create(Predicate<E> param0, Function<E, Optional<? extends LivingEntity>> param1) {
        return BehaviorBuilder.create(
            param2 -> param2.<MemoryAccessor, MemoryAccessor>group(
                        param2.absent(MemoryModuleType.ATTACK_TARGET), param2.registered(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)
                    )
                    .apply(param2, (param2x, param3) -> (param4, param5, param6) -> {
                            if (!param0.test(param5)) {
                                return false;
                            } else {
                                Optional<? extends LivingEntity> var0x = param1.apply(param5);
                                if (var0x.isEmpty()) {
                                    return false;
                                } else {
                                    LivingEntity var1x = (LivingEntity)var0x.get();
                                    if (!param5.canAttack(var1x)) {
                                        return false;
                                    } else {
                                        param2x.set(var1x);
                                        param3.erase();
                                        return true;
                                    }
                                }
                            }
                        })
        );
    }
}

package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class StopAttackingIfTargetInvalid {
    private static final int TIMEOUT_TO_GET_WITHIN_ATTACK_RANGE = 200;

    public static <E extends Mob> BehaviorControl<E> create(BiConsumer<E, LivingEntity> param0) {
        return create(param0x -> false, param0, true);
    }

    public static <E extends Mob> BehaviorControl<E> create(Predicate<LivingEntity> param0) {
        return create(param0, (param0x, param1) -> {
        }, true);
    }

    public static <E extends Mob> BehaviorControl<E> create() {
        return create(param0 -> false, (param0, param1) -> {
        }, true);
    }

    public static <E extends Mob> BehaviorControl<E> create(Predicate<LivingEntity> param0, BiConsumer<E, LivingEntity> param1, boolean param2) {
        return BehaviorBuilder.create(
            param3 -> param3.<MemoryAccessor, MemoryAccessor>group(
                        param3.present(MemoryModuleType.ATTACK_TARGET), param3.registered(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)
                    )
                    .apply(
                        param3,
                        (param4, param5) -> (param6, param7, param8) -> {
                                LivingEntity var0x = param3.get(param4);
                                if (param7.canAttack(var0x)
                                    && (!param2 || !isTiredOfTryingToReachTarget(param7, param3.tryGet(param5)))
                                    && var0x.isAlive()
                                    && var0x.level == param7.level
                                    && !param0.test(var0x)) {
                                    return true;
                                } else {
                                    param1.accept(param7, var0x);
                                    param4.erase();
                                    return true;
                                }
                            }
                    )
        );
    }

    private static boolean isTiredOfTryingToReachTarget(LivingEntity param0, Optional<Long> param1) {
        return param1.isPresent() && param0.level.getGameTime() - param1.get() > 200L;
    }
}

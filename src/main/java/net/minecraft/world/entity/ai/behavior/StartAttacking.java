package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class StartAttacking<E extends Mob> extends Behavior<E> {
    private final Predicate<E> canAttackPredicate;
    private final Function<E, Optional<? extends LivingEntity>> targetFinderFunction;

    public StartAttacking(Predicate<E> param0, Function<E, Optional<? extends LivingEntity>> param1) {
        super(
            ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryStatus.REGISTERED)
        );
        this.canAttackPredicate = param0;
        this.targetFinderFunction = param1;
    }

    public StartAttacking(Function<E, Optional<? extends LivingEntity>> param0) {
        this(param0x -> true, param0);
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, E param1) {
        if (!this.canAttackPredicate.test(param1)) {
            return false;
        } else {
            Optional<? extends LivingEntity> var0 = this.targetFinderFunction.apply(param1);
            return var0.isPresent() && var0.get().isAlive();
        }
    }

    protected void start(ServerLevel param0, E param1, long param2) {
        this.targetFinderFunction.apply(param1).ifPresent(param1x -> this.setAttackTarget(param1, param1x));
    }

    private void setAttackTarget(E param0, LivingEntity param1) {
        param0.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, param1);
        param0.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
    }
}

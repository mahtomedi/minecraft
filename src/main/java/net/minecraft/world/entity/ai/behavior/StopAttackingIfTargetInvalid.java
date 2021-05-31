package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class StopAttackingIfTargetInvalid<E extends Mob> extends Behavior<E> {
    private static final int TIMEOUT_TO_GET_WITHIN_ATTACK_RANGE = 200;
    private final Predicate<LivingEntity> stopAttackingWhen;
    private final Consumer<E> onTargetErased;

    public StopAttackingIfTargetInvalid(Predicate<LivingEntity> param0, Consumer<E> param1) {
        super(
            ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryStatus.REGISTERED)
        );
        this.stopAttackingWhen = param0;
        this.onTargetErased = param1;
    }

    public StopAttackingIfTargetInvalid(Predicate<LivingEntity> param0) {
        this(param0, param0x -> {
        });
    }

    public StopAttackingIfTargetInvalid(Consumer<E> param0) {
        this(param0x -> false, param0);
    }

    public StopAttackingIfTargetInvalid() {
        this(param0 -> false, param0 -> {
        });
    }

    protected void start(ServerLevel param0, E param1, long param2) {
        LivingEntity var0 = this.getAttackTarget(param1);
        if (!param1.canAttack(var0)) {
            this.clearAttackTarget(param1);
        } else if (isTiredOfTryingToReachTarget(param1)) {
            this.clearAttackTarget(param1);
        } else if (this.isCurrentTargetDeadOrRemoved(param1)) {
            this.clearAttackTarget(param1);
        } else if (this.isCurrentTargetInDifferentLevel(param1)) {
            this.clearAttackTarget(param1);
        } else if (this.stopAttackingWhen.test(this.getAttackTarget(param1))) {
            this.clearAttackTarget(param1);
        }
    }

    private boolean isCurrentTargetInDifferentLevel(E param0) {
        return this.getAttackTarget(param0).level != param0.level;
    }

    private LivingEntity getAttackTarget(E param0) {
        return param0.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
    }

    private static <E extends LivingEntity> boolean isTiredOfTryingToReachTarget(E param0) {
        Optional<Long> var0 = param0.getBrain().getMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        return var0.isPresent() && param0.level.getGameTime() - var0.get() > 200L;
    }

    private boolean isCurrentTargetDeadOrRemoved(E param0) {
        Optional<LivingEntity> var0 = param0.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
        return var0.isPresent() && !var0.get().isAlive();
    }

    protected void clearAttackTarget(E param0) {
        this.onTargetErased.accept(param0);
        param0.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
    }
}

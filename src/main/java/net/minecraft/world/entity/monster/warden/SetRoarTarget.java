package net.minecraft.world.entity.monster.warden;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class SetRoarTarget<E extends Warden> extends Behavior<E> {
    private final Function<E, Optional<? extends LivingEntity>> targetFinderFunction;

    public SetRoarTarget(Function<E, Optional<? extends LivingEntity>> param0) {
        super(
            ImmutableMap.of(
                MemoryModuleType.ROAR_TARGET,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.ATTACK_TARGET,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
                MemoryStatus.REGISTERED
            )
        );
        this.targetFinderFunction = param0;
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, E param1) {
        return this.targetFinderFunction.apply(param1).filter(param1::canTargetEntity).isPresent();
    }

    protected void start(ServerLevel param0, E param1, long param2) {
        this.targetFinderFunction.apply(param1).ifPresent(param1x -> {
            param1.getBrain().setMemory(MemoryModuleType.ROAR_TARGET, param1x);
            param1.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        });
    }
}

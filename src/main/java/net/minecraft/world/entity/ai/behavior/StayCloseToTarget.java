package net.minecraft.world.entity.ai.behavior;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class StayCloseToTarget<E extends LivingEntity> extends Behavior<E> {
    private final Function<LivingEntity, Optional<PositionTracker>> targetPositionGetter;
    private final int closeEnough;
    private final float speedModifier;

    public StayCloseToTarget(Function<LivingEntity, Optional<PositionTracker>> param0, int param1, float param2) {
        super(Map.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));
        this.targetPositionGetter = param0;
        this.closeEnough = param1;
        this.speedModifier = param2;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel param0, E param1) {
        Optional<PositionTracker> var0 = this.targetPositionGetter.apply(param1);
        return var0.isPresent() && !param1.position().closerThan(var0.get().currentPosition(), (double)this.closeEnough);
    }

    @Override
    protected void start(ServerLevel param0, E param1, long param2) {
        BehaviorUtils.setWalkAndLookTargetMemories(param1, this.targetPositionGetter.apply(param1).get(), this.speedModifier, this.closeEnough / 2);
    }
}

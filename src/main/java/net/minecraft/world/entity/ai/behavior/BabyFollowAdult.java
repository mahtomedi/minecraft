package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.IntRange;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class BabyFollowAdult<E extends AgableMob> extends Behavior<E> {
    private final IntRange followRange;
    private final float speedModifier;

    public BabyFollowAdult(IntRange param0, float param1) {
        super(ImmutableMap.of(MemoryModuleType.NEAREST_VISIBLE_ADULT, MemoryStatus.VALUE_PRESENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));
        this.followRange = param0;
        this.speedModifier = param1;
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, E param1) {
        if (!param1.isBaby()) {
            return false;
        } else {
            AgableMob var0 = this.getNearestAdult(param1);
            return param1.closerThan(var0, (double)(this.followRange.getMaxInclusive() + 1))
                && !param1.closerThan(var0, (double)this.followRange.getMinInclusive());
        }
    }

    protected void start(ServerLevel param0, E param1, long param2) {
        BehaviorUtils.setWalkAndLookTargetMemories(param1, this.getNearestAdult(param1), this.speedModifier, this.followRange.getMinInclusive() - 1);
    }

    private AgableMob getNearestAdult(E param0) {
        return param0.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT).get();
    }
}

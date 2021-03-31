package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.function.Function;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class BabyFollowAdult<E extends AgeableMob> extends Behavior<E> {
    private final UniformInt followRange;
    private final Function<LivingEntity, Float> speedModifier;

    public BabyFollowAdult(UniformInt param0, float param1) {
        this(param0, param1x -> param1);
    }

    public BabyFollowAdult(UniformInt param0, Function<LivingEntity, Float> param1) {
        super(ImmutableMap.of(MemoryModuleType.NEAREST_VISIBLE_ADULT, MemoryStatus.VALUE_PRESENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));
        this.followRange = param0;
        this.speedModifier = param1;
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, E param1) {
        if (!param1.isBaby()) {
            return false;
        } else {
            AgeableMob var0 = this.getNearestAdult(param1);
            return param1.closerThan(var0, (double)(this.followRange.getMaxValue() + 1)) && !param1.closerThan(var0, (double)this.followRange.getMinValue());
        }
    }

    protected void start(ServerLevel param0, E param1, long param2) {
        BehaviorUtils.setWalkAndLookTargetMemories(param1, this.getNearestAdult(param1), this.speedModifier.apply(param1), this.followRange.getMinValue() - 1);
    }

    private AgeableMob getNearestAdult(E param0) {
        return param0.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT).get();
    }
}

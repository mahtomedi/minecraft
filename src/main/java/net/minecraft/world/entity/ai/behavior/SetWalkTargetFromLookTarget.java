package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class SetWalkTargetFromLookTarget extends Behavior<LivingEntity> {
    private final Function<LivingEntity, Float> speedModifier;
    private final int closeEnoughDistance;
    private final Predicate<LivingEntity> canSetWalkTargetPredicate;

    public SetWalkTargetFromLookTarget(float param0, int param1) {
        this(param0x -> true, param1x -> param0, param1);
    }

    public SetWalkTargetFromLookTarget(Predicate<LivingEntity> param0, Function<LivingEntity, Float> param1, int param2) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_PRESENT));
        this.speedModifier = param1;
        this.closeEnoughDistance = param2;
        this.canSetWalkTargetPredicate = param0;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel param0, LivingEntity param1) {
        return this.canSetWalkTargetPredicate.test(param1);
    }

    @Override
    protected void start(ServerLevel param0, LivingEntity param1, long param2) {
        Brain<?> var0 = param1.getBrain();
        PositionTracker var1 = var0.getMemory(MemoryModuleType.LOOK_TARGET).get();
        var0.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(var1, this.speedModifier.apply(param1), this.closeEnoughDistance));
    }
}

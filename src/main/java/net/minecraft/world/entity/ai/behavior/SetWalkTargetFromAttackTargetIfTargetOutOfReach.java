package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.function.Function;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class SetWalkTargetFromAttackTargetIfTargetOutOfReach extends Behavior<Mob> {
    private static final int PROJECTILE_ATTACK_RANGE_BUFFER = 1;
    private final Function<LivingEntity, Float> speedModifier;

    public SetWalkTargetFromAttackTargetIfTargetOutOfReach(float param0) {
        this(param1 -> param0);
    }

    public SetWalkTargetFromAttackTargetIfTargetOutOfReach(Function<LivingEntity, Float> param0) {
        super(
            ImmutableMap.of(
                MemoryModuleType.WALK_TARGET,
                MemoryStatus.REGISTERED,
                MemoryModuleType.LOOK_TARGET,
                MemoryStatus.REGISTERED,
                MemoryModuleType.ATTACK_TARGET,
                MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
                MemoryStatus.REGISTERED
            )
        );
        this.speedModifier = param0;
    }

    protected void start(ServerLevel param0, Mob param1, long param2) {
        LivingEntity var0 = param1.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
        if (BehaviorUtils.canSee(param1, var0) && BehaviorUtils.isWithinAttackRange(param1, var0, 1)) {
            this.clearWalkTarget(param1);
        } else {
            this.setWalkAndLookTarget(param1, var0);
        }

    }

    private void setWalkAndLookTarget(LivingEntity param0, LivingEntity param1) {
        Brain<?> var0 = param0.getBrain();
        var0.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(param1, true));
        WalkTarget var1 = new WalkTarget(new EntityTracker(param1, false), this.speedModifier.apply(param0), 0);
        var0.setMemory(MemoryModuleType.WALK_TARGET, var1);
    }

    private void clearWalkTarget(LivingEntity param0) {
        param0.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }
}

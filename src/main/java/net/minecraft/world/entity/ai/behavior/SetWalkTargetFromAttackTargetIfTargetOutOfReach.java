package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class SetWalkTargetFromAttackTargetIfTargetOutOfReach extends Behavior<Mob> {
    private final float speedModifier;

    public SetWalkTargetFromAttackTargetIfTargetOutOfReach(float param0) {
        super(
            ImmutableMap.of(
                MemoryModuleType.WALK_TARGET,
                MemoryStatus.REGISTERED,
                MemoryModuleType.LOOK_TARGET,
                MemoryStatus.REGISTERED,
                MemoryModuleType.ATTACK_TARGET,
                MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.VISIBLE_LIVING_ENTITIES,
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
        Brain var0 = param0.getBrain();
        PositionWrapper var1 = new EntityPosWrapper(param1);
        var0.setMemory(MemoryModuleType.LOOK_TARGET, var1);
        WalkTarget var2 = new WalkTarget(var1, this.speedModifier, 0);
        var0.setMemory(MemoryModuleType.WALK_TARGET, var2);
    }

    private void clearWalkTarget(LivingEntity param0) {
        param0.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }
}

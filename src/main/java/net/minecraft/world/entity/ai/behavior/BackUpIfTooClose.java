package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class BackUpIfTooClose<E extends Mob> extends Behavior<E> {
    private final int tooCloseDistance;
    private final float strafeSpeed;

    public BackUpIfTooClose(int param0, float param1) {
        super(
            ImmutableMap.of(
                MemoryModuleType.WALK_TARGET,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.LOOK_TARGET,
                MemoryStatus.REGISTERED,
                MemoryModuleType.ATTACK_TARGET,
                MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
                MemoryStatus.VALUE_PRESENT
            )
        );
        this.tooCloseDistance = param0;
        this.strafeSpeed = param1;
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, E param1) {
        return this.isTargetVisible(param1) && this.isTargetTooClose(param1);
    }

    protected void start(ServerLevel param0, E param1, long param2) {
        param1.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(this.getTarget(param1), true));
        param1.getMoveControl().strafe(-this.strafeSpeed, 0.0F);
        param1.setYRot(Mth.rotateIfNecessary(param1.getYRot(), param1.yHeadRot, 0.0F));
    }

    private boolean isTargetVisible(E param0) {
        return param0.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get().contains(this.getTarget(param0));
    }

    private boolean isTargetTooClose(E param0) {
        return this.getTarget(param0).closerThan(param0, (double)this.tooCloseDistance);
    }

    private LivingEntity getTarget(E param0) {
        return param0.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
    }
}

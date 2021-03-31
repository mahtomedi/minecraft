package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class LongJumpMidJump extends Behavior<Mob> {
    public static final int TIME_OUT_DURATION = 100;
    private final UniformInt timeBetweenLongJumps;

    public LongJumpMidJump(UniformInt param0) {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryStatus.VALUE_PRESENT), 100);
        this.timeBetweenLongJumps = param0;
    }

    protected boolean canStillUse(ServerLevel param0, Mob param1, long param2) {
        return !param1.isOnGround();
    }

    protected void start(ServerLevel param0, Mob param1, long param2) {
        param1.setDiscardFriction(true);
        param1.setPose(Pose.LONG_JUMPING);
    }

    protected void stop(ServerLevel param0, Mob param1, long param2) {
        if (param1.isOnGround()) {
            param1.setDeltaMovement(param1.getDeltaMovement().scale(0.1F));
        }

        param1.setDiscardFriction(false);
        param1.setPose(Pose.STANDING);
        param1.getBrain().eraseMemory(MemoryModuleType.LONG_JUMP_MID_JUMP);
        param1.getBrain().setMemory(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, this.timeBetweenLongJumps.sample(param0.random));
    }
}
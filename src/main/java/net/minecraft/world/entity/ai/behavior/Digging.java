package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.warden.Warden;

public class Digging<E extends Warden> extends Behavior<E> {
    public Digging(int param0) {
        super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT), param0);
    }

    protected boolean canStillUse(ServerLevel param0, E param1, long param2) {
        return true;
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, E param1) {
        return param1.isOnGround();
    }

    protected void start(ServerLevel param0, E param1, long param2) {
        param1.setPose(Pose.DIGGING);
        param1.playSound(SoundEvents.WARDEN_DIG, 5.0F, 1.0F);
    }

    protected void stop(ServerLevel param0, E param1, long param2) {
        param1.remove(Entity.RemovalReason.DISCARDED);
    }
}

package net.minecraft.world.entity.ai.behavior.warden;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.warden.Warden;

public class Digging<E extends Warden> extends Behavior<E> {
    public Digging(int param0) {
        super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT), param0);
    }

    protected boolean canStillUse(ServerLevel param0, E param1, long param2) {
        return param1.getRemovalReason() == null;
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, E param1) {
        return param1.onGround() || param1.isInWater() || param1.isInLava();
    }

    protected void start(ServerLevel param0, E param1, long param2) {
        if (param1.onGround()) {
            param1.setPose(Pose.DIGGING);
            param1.playSound(SoundEvents.WARDEN_DIG, 5.0F, 1.0F);
        } else {
            param1.playSound(SoundEvents.WARDEN_AGITATED, 5.0F, 1.0F);
            this.stop(param0, param1, param2);
        }

    }

    protected void stop(ServerLevel param0, E param1, long param2) {
        if (param1.getRemovalReason() == null) {
            param1.remove(Entity.RemovalReason.DISCARDED);
        }

    }
}

package net.minecraft.world.entity.ai.behavior.warden;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.warden.Warden;

public class Emerging<E extends Warden> extends Behavior<E> {
    public Emerging(int param0) {
        super(
            ImmutableMap.of(
                MemoryModuleType.IS_EMERGING,
                MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.WALK_TARGET,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.LOOK_TARGET,
                MemoryStatus.REGISTERED
            ),
            param0
        );
    }

    protected boolean canStillUse(ServerLevel param0, E param1, long param2) {
        return true;
    }

    protected void start(ServerLevel param0, E param1, long param2) {
        param1.setPose(Pose.EMERGING);
        param1.playSound(SoundEvents.WARDEN_EMERGE, 5.0F, 1.0F);
    }

    protected void stop(ServerLevel param0, E param1, long param2) {
        if (param1.hasPose(Pose.EMERGING)) {
            param1.setPose(Pose.STANDING);
        }

    }
}

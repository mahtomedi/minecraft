package net.minecraft.world.entity.ai.behavior.warden;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.warden.WardenAi;

public class Sniffing<E extends Warden> extends Behavior<E> {
    private static final double ANGER_FROM_SNIFFING_MAX_DISTANCE_XZ = 6.0;
    private static final double ANGER_FROM_SNIFFING_MAX_DISTANCE_Y = 20.0;

    public Sniffing(int param0) {
        super(
            ImmutableMap.of(
                MemoryModuleType.IS_SNIFFING,
                MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.ATTACK_TARGET,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.WALK_TARGET,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.LOOK_TARGET,
                MemoryStatus.REGISTERED,
                MemoryModuleType.NEAREST_ATTACKABLE,
                MemoryStatus.REGISTERED
            ),
            param0
        );
    }

    protected boolean canStillUse(ServerLevel param0, E param1, long param2) {
        return true;
    }

    protected void start(ServerLevel param0, E param1, long param2) {
        param1.playSound(SoundEvents.WARDEN_SNIFF, 5.0F, 1.0F);
    }

    protected void stop(ServerLevel param0, E param1, long param2) {
        if (param1.hasPose(Pose.SNIFFING)) {
            param1.setPose(Pose.STANDING);
        }

        param1.getBrain().eraseMemory(MemoryModuleType.IS_SNIFFING);
        param1.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE).filter(param1::canTargetEntity).ifPresent(param1x -> {
            if (param1.closerThan(param1x, 6.0, 20.0)) {
                param1.increaseAngerAt(param1x);
            }

            WardenAi.setDisturbanceLocation(param1, param1x.blockPosition());
        });
    }
}

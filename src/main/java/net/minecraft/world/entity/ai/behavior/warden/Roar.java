package net.minecraft.world.entity.ai.behavior.warden;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.warden.WardenAi;

public class Roar extends Behavior<Warden> {
    private static final int TICKS_BEFORE_PLAYING_ROAR_SOUND = 25;

    public Roar() {
        super(
            ImmutableMap.of(
                MemoryModuleType.ROAR_TARGET,
                MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.ATTACK_TARGET,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.ROAR_SOUND_COOLDOWN,
                MemoryStatus.REGISTERED,
                MemoryModuleType.ROAR_SOUND_DELAY,
                MemoryStatus.REGISTERED
            ),
            WardenAi.ROAR_DURATION
        );
    }

    protected void start(ServerLevel param0, Warden param1, long param2) {
        Brain<Warden> var0 = param1.getBrain();
        var0.setMemoryWithExpiry(MemoryModuleType.ROAR_SOUND_DELAY, Unit.INSTANCE, 25L);
        var0.eraseMemory(MemoryModuleType.WALK_TARGET);
        BehaviorUtils.lookAtEntity(param1, param1.getBrain().getMemory(MemoryModuleType.ROAR_TARGET).get());
        param1.setPose(Pose.ROARING);
    }

    protected boolean canStillUse(ServerLevel param0, Warden param1, long param2) {
        return true;
    }

    protected void tick(ServerLevel param0, Warden param1, long param2) {
        if (!param1.getBrain().hasMemoryValue(MemoryModuleType.ROAR_SOUND_DELAY) && !param1.getBrain().hasMemoryValue(MemoryModuleType.ROAR_SOUND_COOLDOWN)) {
            param1.getBrain().setMemoryWithExpiry(MemoryModuleType.ROAR_SOUND_COOLDOWN, Unit.INSTANCE, (long)(WardenAi.ROAR_DURATION - 25));
            param1.playSound(SoundEvents.WARDEN_ROAR, 3.0F, 1.0F);
        }
    }

    protected void stop(ServerLevel param0, Warden param1, long param2) {
        if (param1.hasPose(Pose.ROARING)) {
            param1.setPose(Pose.STANDING);
        }

        param1.getBrain().getMemory(MemoryModuleType.ROAR_TARGET).ifPresent(param1::setAttackTarget);
        param1.getBrain().eraseMemory(MemoryModuleType.ROAR_TARGET);
    }
}
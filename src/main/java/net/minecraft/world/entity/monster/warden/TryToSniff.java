package net.minecraft.world.entity.monster.warden;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class TryToSniff extends Behavior<Warden> {
    private static final int SNIFF_COOLDOWN = 120;

    public TryToSniff() {
        super(
            ImmutableMap.of(
                MemoryModuleType.SNIFF_COOLDOWN,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.NEAREST_ATTACKABLE,
                MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.DISTURBANCE_LOCATION,
                MemoryStatus.VALUE_ABSENT
            )
        );
    }

    protected void start(ServerLevel param0, Warden param1, long param2) {
        Brain<Warden> var0 = param1.getBrain();
        var0.setMemory(MemoryModuleType.IS_SNIFFING, Unit.INSTANCE);
        var0.setMemoryWithExpiry(MemoryModuleType.SNIFF_COOLDOWN, Unit.INSTANCE, 120L);
        var0.eraseMemory(MemoryModuleType.WALK_TARGET);
        param1.setPose(Pose.SNIFFING);
    }
}

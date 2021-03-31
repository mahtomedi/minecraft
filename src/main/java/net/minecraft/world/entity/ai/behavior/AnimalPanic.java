package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

public class AnimalPanic extends Behavior<PathfinderMob> {
    private static final int PANIC_MIN_DURATION = 100;
    private static final int PANIC_MAX_DURATION = 120;
    private static final int PANIC_DISTANCE_HORIZANTAL = 5;
    private static final int PANIC_DISTANCE_VERTICAL = 4;
    private final float speedMultiplier;

    public AnimalPanic(float param0) {
        super(ImmutableMap.of(MemoryModuleType.HURT_BY, MemoryStatus.VALUE_PRESENT), 100, 120);
        this.speedMultiplier = param0;
    }

    protected boolean canStillUse(ServerLevel param0, PathfinderMob param1, long param2) {
        return true;
    }

    protected void start(ServerLevel param0, PathfinderMob param1, long param2) {
        param1.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }

    protected void tick(ServerLevel param0, PathfinderMob param1, long param2) {
        if (param1.getNavigation().isDone()) {
            Vec3 var0 = LandRandomPos.getPos(param1, 5, 4);
            if (var0 != null) {
                param1.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(var0, this.speedMultiplier, 0));
            }
        }

    }
}

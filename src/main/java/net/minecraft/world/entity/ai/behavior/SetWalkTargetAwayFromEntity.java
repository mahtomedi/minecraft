package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.phys.Vec3;

public class SetWalkTargetAwayFromEntity extends Behavior<PathfinderMob> {
    private final MemoryModuleType<? extends Entity> memory;
    private final float speed;

    public SetWalkTargetAwayFromEntity(MemoryModuleType<? extends Entity> param0, float param1) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, param0, MemoryStatus.VALUE_PRESENT));
        this.memory = param0;
        this.speed = param1;
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, PathfinderMob param1) {
        Entity var0 = param1.getBrain().getMemory(this.memory).get();
        return param1.distanceToSqr(var0) < 36.0;
    }

    protected void start(ServerLevel param0, PathfinderMob param1, long param2) {
        Entity var0 = param1.getBrain().getMemory(this.memory).get();
        moveAwayFromMob(param1, var0, this.speed);
    }

    public static void moveAwayFromMob(PathfinderMob param0, Entity param1, float param2) {
        for(int var0 = 0; var0 < 10; ++var0) {
            Vec3 var1 = RandomPos.getLandPosAvoid(param0, 16, 7, param1.position());
            if (var1 != null) {
                param0.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(var1, param2, 0));
                return;
            }
        }

    }
}

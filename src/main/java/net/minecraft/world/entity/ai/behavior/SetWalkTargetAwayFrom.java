package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

public class SetWalkTargetAwayFrom<T> extends Behavior<PathfinderMob> {
    private final MemoryModuleType<T> walkAwayFromMemory;
    private final float speedModifier;
    private final int desiredDistance;
    private final Function<T, Vec3> toPosition;

    public SetWalkTargetAwayFrom(MemoryModuleType<T> param0, float param1, int param2, boolean param3, Function<T, Vec3> param4) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, param3 ? MemoryStatus.REGISTERED : MemoryStatus.VALUE_ABSENT, param0, MemoryStatus.VALUE_PRESENT));
        this.walkAwayFromMemory = param0;
        this.speedModifier = param1;
        this.desiredDistance = param2;
        this.toPosition = param4;
    }

    public static SetWalkTargetAwayFrom<BlockPos> pos(MemoryModuleType<BlockPos> param0, float param1, int param2, boolean param3) {
        return new SetWalkTargetAwayFrom<>(param0, param1, param2, param3, Vec3::atBottomCenterOf);
    }

    public static SetWalkTargetAwayFrom<? extends Entity> entity(MemoryModuleType<? extends Entity> param0, float param1, int param2, boolean param3) {
        return new SetWalkTargetAwayFrom<>(param0, param1, param2, param3, Entity::position);
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, PathfinderMob param1) {
        return this.alreadyWalkingAwayFromPosWithSameSpeed(param1)
            ? false
            : param1.position().closerThan(this.getPosToAvoid(param1), (double)this.desiredDistance);
    }

    private Vec3 getPosToAvoid(PathfinderMob param0) {
        return this.toPosition.apply(param0.getBrain().getMemory(this.walkAwayFromMemory).get());
    }

    private boolean alreadyWalkingAwayFromPosWithSameSpeed(PathfinderMob param0) {
        if (!param0.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET)) {
            return false;
        } else {
            WalkTarget var0 = param0.getBrain().getMemory(MemoryModuleType.WALK_TARGET).get();
            if (var0.getSpeedModifier() != this.speedModifier) {
                return false;
            } else {
                Vec3 var1 = var0.getTarget().currentPosition().subtract(param0.position());
                Vec3 var2 = this.getPosToAvoid(param0).subtract(param0.position());
                return var1.dot(var2) < 0.0;
            }
        }
    }

    protected void start(ServerLevel param0, PathfinderMob param1, long param2) {
        moveAwayFrom(param1, this.getPosToAvoid(param1), this.speedModifier);
    }

    private static void moveAwayFrom(PathfinderMob param0, Vec3 param1, float param2) {
        for(int var0 = 0; var0 < 10; ++var0) {
            Vec3 var1 = LandRandomPos.getPosAway(param0, 16, 7, param1);
            if (var1 != null) {
                param0.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(var1, param2, 0));
                return;
            }
        }

    }
}

package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;

public class AnimalPanic extends Behavior<PathfinderMob> {
    private static final int PANIC_MIN_DURATION = 100;
    private static final int PANIC_MAX_DURATION = 120;
    private static final int PANIC_DISTANCE_HORIZONTAL = 5;
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
            Vec3 var0 = this.getPanicPos(param1, param0);
            if (var0 != null) {
                param1.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(var0, this.speedMultiplier, 0));
            }
        }

    }

    @Nullable
    private Vec3 getPanicPos(PathfinderMob param0, ServerLevel param1) {
        if (param0.isOnFire()) {
            Optional<Vec3> var0 = this.lookForWater(param1, param0).map(Vec3::atBottomCenterOf);
            if (var0.isPresent()) {
                return var0.get();
            }
        }

        return LandRandomPos.getPos(param0, 5, 4);
    }

    private Optional<BlockPos> lookForWater(BlockGetter param0, Entity param1) {
        BlockPos var0 = param1.blockPosition();
        return !param0.getBlockState(var0).getCollisionShape(param0, var0).isEmpty()
            ? Optional.empty()
            : BlockPos.findClosestMatch(var0, 5, 1, param1x -> param0.getFluidState(param1x).is(FluidTags.WATER));
    }
}

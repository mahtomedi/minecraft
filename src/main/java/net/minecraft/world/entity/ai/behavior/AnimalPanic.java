package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
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
    private static final Predicate<PathfinderMob> DEFAULT_SHOULD_PANIC_PREDICATE = param0 -> param0.getLastHurtByMob() != null
            || param0.isFreezing()
            || param0.isOnFire();
    private final float speedMultiplier;
    private final Predicate<PathfinderMob> shouldPanic;

    public AnimalPanic(float param0) {
        this(param0, DEFAULT_SHOULD_PANIC_PREDICATE);
    }

    public AnimalPanic(float param0, Predicate<PathfinderMob> param1) {
        super(ImmutableMap.of(MemoryModuleType.IS_PANICKING, MemoryStatus.REGISTERED, MemoryModuleType.HURT_BY, MemoryStatus.VALUE_PRESENT), 100, 120);
        this.speedMultiplier = param0;
        this.shouldPanic = param1;
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, PathfinderMob param1) {
        return this.shouldPanic.test(param1);
    }

    protected boolean canStillUse(ServerLevel param0, PathfinderMob param1, long param2) {
        return true;
    }

    protected void start(ServerLevel param0, PathfinderMob param1, long param2) {
        param1.getBrain().setMemory(MemoryModuleType.IS_PANICKING, true);
        param1.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }

    protected void stop(ServerLevel param0, PathfinderMob param1, long param2) {
        Brain<?> var0 = param1.getBrain();
        var0.eraseMemory(MemoryModuleType.IS_PANICKING);
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
        if (!param0.getBlockState(var0).getCollisionShape(param0, var0).isEmpty()) {
            return Optional.empty();
        } else {
            Predicate<BlockPos> var1;
            if (Mth.ceil(param1.getBbWidth()) == 2) {
                var1 = param1x -> BlockPos.squareOutSouthEast(param1x).allMatch(param1xx -> param0.getFluidState(param1xx).is(FluidTags.WATER));
            } else {
                var1 = param1x -> param0.getFluidState(param1x).is(FluidTags.WATER);
            }

            return BlockPos.findClosestMatch(var0, 5, 1, var1);
        }
    }
}

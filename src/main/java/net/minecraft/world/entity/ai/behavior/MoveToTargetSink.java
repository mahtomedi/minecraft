package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class MoveToTargetSink extends Behavior<Mob> {
    private int remainingCooldown;
    @Nullable
    private Path path;
    @Nullable
    private BlockPos lastTargetPos;
    private float speedModifier;

    public MoveToTargetSink() {
        this(150, 250);
    }

    public MoveToTargetSink(int param0, int param1) {
        super(
            ImmutableMap.of(
                MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
                MemoryStatus.REGISTERED,
                MemoryModuleType.PATH,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.WALK_TARGET,
                MemoryStatus.VALUE_PRESENT
            ),
            param0,
            param1
        );
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, Mob param1) {
        if (this.remainingCooldown > 0) {
            --this.remainingCooldown;
            return false;
        } else {
            Brain<?> var0 = param1.getBrain();
            WalkTarget var1 = var0.getMemory(MemoryModuleType.WALK_TARGET).get();
            boolean var2 = this.reachedTarget(param1, var1);
            if (!var2 && this.tryComputePath(param1, var1, param0.getGameTime())) {
                this.lastTargetPos = var1.getTarget().currentBlockPosition();
                return true;
            } else {
                var0.eraseMemory(MemoryModuleType.WALK_TARGET);
                if (var2) {
                    var0.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
                }

                return false;
            }
        }
    }

    protected boolean canStillUse(ServerLevel param0, Mob param1, long param2) {
        if (this.path != null && this.lastTargetPos != null) {
            Optional<WalkTarget> var0 = param1.getBrain().getMemory(MemoryModuleType.WALK_TARGET);
            PathNavigation var1 = param1.getNavigation();
            return !var1.isDone() && var0.isPresent() && !this.reachedTarget(param1, var0.get());
        } else {
            return false;
        }
    }

    protected void stop(ServerLevel param0, Mob param1, long param2) {
        if (param1.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET)
            && !this.reachedTarget(param1, param1.getBrain().getMemory(MemoryModuleType.WALK_TARGET).get())
            && param1.getNavigation().isStuck()) {
            this.remainingCooldown = param0.getRandom().nextInt(40);
        }

        param1.getNavigation().stop();
        param1.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        param1.getBrain().eraseMemory(MemoryModuleType.PATH);
        this.path = null;
    }

    protected void start(ServerLevel param0, Mob param1, long param2) {
        param1.getBrain().setMemory(MemoryModuleType.PATH, this.path);
        param1.getNavigation().moveTo(this.path, (double)this.speedModifier);
    }

    protected void tick(ServerLevel param0, Mob param1, long param2) {
        Path var0 = param1.getNavigation().getPath();
        Brain<?> var1 = param1.getBrain();
        if (this.path != var0) {
            this.path = var0;
            var1.setMemory(MemoryModuleType.PATH, var0);
        }

        if (var0 != null && this.lastTargetPos != null) {
            WalkTarget var2 = var1.getMemory(MemoryModuleType.WALK_TARGET).get();
            if (var2.getTarget().currentBlockPosition().distSqr(this.lastTargetPos) > 4.0 && this.tryComputePath(param1, var2, param0.getGameTime())) {
                this.lastTargetPos = var2.getTarget().currentBlockPosition();
                this.start(param0, param1, param2);
            }

        }
    }

    private boolean tryComputePath(Mob param0, WalkTarget param1, long param2) {
        BlockPos var0 = param1.getTarget().currentBlockPosition();
        this.path = param0.getNavigation().createPath(var0, 0);
        this.speedModifier = param1.getSpeedModifier();
        Brain<?> var1 = param0.getBrain();
        if (this.reachedTarget(param0, param1)) {
            var1.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        } else {
            boolean var2 = this.path != null && this.path.canReach();
            if (var2) {
                var1.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
            } else if (!var1.hasMemoryValue(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)) {
                var1.setMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, param2);
            }

            if (this.path != null) {
                return true;
            }

            Vec3 var3 = RandomPos.getPosTowards((PathfinderMob)param0, 10, 7, Vec3.atBottomCenterOf(var0));
            if (var3 != null) {
                this.path = param0.getNavigation().createPath(var3.x, var3.y, var3.z, 0);
                return this.path != null;
            }
        }

        return false;
    }

    private boolean reachedTarget(Mob param0, WalkTarget param1) {
        return param1.getTarget().currentBlockPosition().distManhattan(param0.blockPosition()) <= param1.getCloseEnoughDist();
    }
}

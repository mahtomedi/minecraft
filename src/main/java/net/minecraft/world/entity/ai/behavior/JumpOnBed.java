package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class JumpOnBed extends Behavior<Mob> {
    private final float speedModifier;
    @Nullable
    private BlockPos targetBed;
    private int remainingTimeToReachBed;
    private int remainingJumps;
    private int remainingCooldownUntilNextJump;

    public JumpOnBed(float param0) {
        super(ImmutableMap.of(MemoryModuleType.NEAREST_BED, MemoryStatus.VALUE_PRESENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));
        this.speedModifier = param0;
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, Mob param1) {
        return param1.isBaby() && this.nearBed(param0, param1);
    }

    protected void start(ServerLevel param0, Mob param1, long param2) {
        super.start(param0, param1, param2);
        this.getNearestBed(param1).ifPresent(param2x -> {
            this.targetBed = param2x;
            this.remainingTimeToReachBed = 100;
            this.remainingJumps = 3 + param0.random.nextInt(4);
            this.remainingCooldownUntilNextJump = 0;
            this.startWalkingTowardsBed(param1, param2x);
        });
    }

    protected void stop(ServerLevel param0, Mob param1, long param2) {
        super.stop(param0, param1, param2);
        this.targetBed = null;
        this.remainingTimeToReachBed = 0;
        this.remainingJumps = 0;
        this.remainingCooldownUntilNextJump = 0;
    }

    protected boolean canStillUse(ServerLevel param0, Mob param1, long param2) {
        return param1.isBaby()
            && this.targetBed != null
            && this.isBed(param0, this.targetBed)
            && !this.tiredOfWalking(param0, param1)
            && !this.tiredOfJumping(param0, param1);
    }

    @Override
    protected boolean timedOut(long param0) {
        return false;
    }

    protected void tick(ServerLevel param0, Mob param1, long param2) {
        if (!this.onOrOverBed(param0, param1)) {
            --this.remainingTimeToReachBed;
        } else if (this.remainingCooldownUntilNextJump > 0) {
            --this.remainingCooldownUntilNextJump;
        } else {
            if (this.onBedSurface(param0, param1)) {
                param1.getJumpControl().jump();
                --this.remainingJumps;
                this.remainingCooldownUntilNextJump = 5;
            }

        }
    }

    private void startWalkingTowardsBed(Mob param0, BlockPos param1) {
        param0.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(param1, this.speedModifier, 0));
    }

    private boolean nearBed(ServerLevel param0, Mob param1) {
        return this.onOrOverBed(param0, param1) || this.getNearestBed(param1).isPresent();
    }

    private boolean onOrOverBed(ServerLevel param0, Mob param1) {
        BlockPos var0 = param1.blockPosition();
        BlockPos var1 = var0.below();
        return this.isBed(param0, var0) || this.isBed(param0, var1);
    }

    private boolean onBedSurface(ServerLevel param0, Mob param1) {
        return this.isBed(param0, param1.blockPosition());
    }

    private boolean isBed(ServerLevel param0, BlockPos param1) {
        return param0.getBlockState(param1).is(BlockTags.BEDS);
    }

    private Optional<BlockPos> getNearestBed(Mob param0) {
        return param0.getBrain().getMemory(MemoryModuleType.NEAREST_BED);
    }

    private boolean tiredOfWalking(ServerLevel param0, Mob param1) {
        return !this.onOrOverBed(param0, param1) && this.remainingTimeToReachBed <= 0;
    }

    private boolean tiredOfJumping(ServerLevel param0, Mob param1) {
        return this.onOrOverBed(param0, param1) && this.remainingJumps <= 0;
    }
}

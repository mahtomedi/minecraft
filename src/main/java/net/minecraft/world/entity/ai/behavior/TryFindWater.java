package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class TryFindWater extends Behavior<PathfinderMob> {
    private final int range;
    private final float speedModifier;
    private long nextOkStartTime;

    public TryFindWater(int param0, float param1) {
        super(
            ImmutableMap.of(
                MemoryModuleType.ATTACK_TARGET,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.WALK_TARGET,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.LOOK_TARGET,
                MemoryStatus.REGISTERED
            )
        );
        this.range = param0;
        this.speedModifier = param1;
    }

    protected void stop(ServerLevel param0, PathfinderMob param1, long param2) {
        this.nextOkStartTime = param2 + 20L + 2L;
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, PathfinderMob param1) {
        return !param1.level.getFluidState(param1.blockPosition()).is(FluidTags.WATER);
    }

    protected void start(ServerLevel param0, PathfinderMob param1, long param2) {
        if (param2 >= this.nextOkStartTime) {
            BlockPos var0 = null;
            BlockPos var1 = null;
            BlockPos var2 = param1.blockPosition();

            for(BlockPos var4 : BlockPos.withinManhattan(var2, this.range, this.range, this.range)) {
                if (var4.getX() != var2.getX() || var4.getZ() != var2.getZ()) {
                    BlockState var5 = param1.level.getBlockState(var4.above());
                    BlockState var6 = param1.level.getBlockState(var4);
                    if (var6.is(Blocks.WATER)) {
                        if (var5.isAir()) {
                            var0 = var4.immutable();
                            break;
                        }

                        if (var1 == null && !var4.closerToCenterThan(param1.position(), 1.5)) {
                            var1 = var4.immutable();
                        }
                    }
                }
            }

            if (var0 == null) {
                var0 = var1;
            }

            if (var0 != null) {
                this.nextOkStartTime = param2 + 40L;
                BehaviorUtils.setWalkAndLookTargetMemories(param1, var0, this.speedModifier, 0);
            }

        }
    }
}

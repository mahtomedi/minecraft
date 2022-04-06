package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;

public class TryFindLand extends Behavior<PathfinderMob> {
    private static final int COOLDOWN_TICKS = 60;
    private final int range;
    private final float speedModifier;
    private long nextOkStartTime;

    public TryFindLand(int param0, float param1) {
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
        this.nextOkStartTime = param2 + 60L;
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, PathfinderMob param1) {
        return param1.level.getFluidState(param1.blockPosition()).is(FluidTags.WATER);
    }

    protected void start(ServerLevel param0, PathfinderMob param1, long param2) {
        if (param2 >= this.nextOkStartTime) {
            BlockPos var0 = param1.blockPosition();
            BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos();
            CollisionContext var2 = CollisionContext.of(param1);

            for(BlockPos var3 : BlockPos.withinManhattan(var0, this.range, this.range, this.range)) {
                if (var3.getX() != var0.getX() || var3.getZ() != var0.getZ()) {
                    BlockState var4 = param0.getBlockState(var3);
                    BlockState var5 = param0.getBlockState(var1.setWithOffset(var3, Direction.DOWN));
                    if (!var4.is(Blocks.WATER)
                        && param0.getFluidState(var3).isEmpty()
                        && var4.getCollisionShape(param0, var3, var2).isEmpty()
                        && var5.isFaceSturdy(param0, var1, Direction.UP)) {
                        this.nextOkStartTime = param2 + 60L;
                        BehaviorUtils.setWalkAndLookTargetMemories(param1, var3.immutable(), this.speedModifier, 1);
                        return;
                    }
                }
            }

        }
    }
}

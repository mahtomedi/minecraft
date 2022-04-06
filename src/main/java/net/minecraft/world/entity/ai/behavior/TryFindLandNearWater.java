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
import net.minecraft.world.phys.shapes.CollisionContext;

public class TryFindLandNearWater extends Behavior<PathfinderMob> {
    private final int range;
    private final float speedModifier;
    private long nextOkStartTime;

    public TryFindLandNearWater(int param0, float param1) {
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
        this.nextOkStartTime = param2 + 40L;
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, PathfinderMob param1) {
        return !param1.level.getFluidState(param1.blockPosition()).is(FluidTags.WATER);
    }

    protected void start(ServerLevel param0, PathfinderMob param1, long param2) {
        if (param2 >= this.nextOkStartTime) {
            CollisionContext var0 = CollisionContext.of(param1);
            BlockPos var1 = param1.blockPosition();
            BlockPos.MutableBlockPos var2 = new BlockPos.MutableBlockPos();

            for(BlockPos var3 : BlockPos.withinManhattan(var1, this.range, this.range, this.range)) {
                if ((var3.getX() != var1.getX() || var3.getZ() != var1.getZ())
                    && param0.getBlockState(var3).getCollisionShape(param0, var3, var0).isEmpty()
                    && !param0.getBlockState(var2.setWithOffset(var3, Direction.DOWN)).getCollisionShape(param0, var3, var0).isEmpty()) {
                    for(Direction var4 : Direction.Plane.HORIZONTAL) {
                        var2.setWithOffset(var3, var4);
                        if (param0.getBlockState(var2).isAir() && param0.getBlockState(var2.move(Direction.DOWN)).is(Blocks.WATER)) {
                            this.nextOkStartTime = param2 + 40L;
                            BehaviorUtils.setWalkAndLookTargetMemories(param1, var3, this.speedModifier, 0);
                            return;
                        }
                    }
                }
            }

        }
    }
}

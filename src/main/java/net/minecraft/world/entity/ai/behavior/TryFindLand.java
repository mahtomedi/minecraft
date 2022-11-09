package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.apache.commons.lang3.mutable.MutableLong;

public class TryFindLand {
    private static final int COOLDOWN_TICKS = 60;

    public static BehaviorControl<PathfinderMob> create(int param0, float param1) {
        MutableLong var0 = new MutableLong(0L);
        return BehaviorBuilder.create(
            param3 -> param3.<MemoryAccessor, MemoryAccessor, MemoryAccessor>group(
                        param3.absent(MemoryModuleType.ATTACK_TARGET),
                        param3.absent(MemoryModuleType.WALK_TARGET),
                        param3.registered(MemoryModuleType.LOOK_TARGET)
                    )
                    .apply(
                        param3,
                        (param3x, param4, param5) -> (param5x, param6, param7) -> {
                                if (!param5x.getFluidState(param6.blockPosition()).is(FluidTags.WATER)) {
                                    return false;
                                } else if (param7 < var0.getValue()) {
                                    var0.setValue(param7 + 60L);
                                    return true;
                                } else {
                                    BlockPos var0x = param6.blockPosition();
                                    BlockPos.MutableBlockPos var1x = new BlockPos.MutableBlockPos();
                                    CollisionContext var2x = CollisionContext.of(param6);
            
                                    for(BlockPos var3x : BlockPos.withinManhattan(var0x, param0, param0, param0)) {
                                        if (var3x.getX() != var0x.getX() || var3x.getZ() != var0x.getZ()) {
                                            BlockState var4x = param5x.getBlockState(var3x);
                                            BlockState var5x = param5x.getBlockState(var1x.setWithOffset(var3x, Direction.DOWN));
                                            if (!var4x.is(Blocks.WATER)
                                                && param5x.getFluidState(var3x).isEmpty()
                                                && var4x.getCollisionShape(param5x, var3x, var2x).isEmpty()
                                                && var5x.isFaceSturdy(param5x, var1x, Direction.UP)) {
                                                BlockPos var6 = var3x.immutable();
                                                param5.set(new BlockPosTracker(var6));
                                                param4.set(new WalkTarget(new BlockPosTracker(var6), param1, 1));
                                                break;
                                            }
                                        }
                                    }
            
                                    var0.setValue(param7 + 60L);
                                    return true;
                                }
                            }
                    )
        );
    }
}

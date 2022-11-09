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
import net.minecraft.world.phys.shapes.CollisionContext;
import org.apache.commons.lang3.mutable.MutableLong;

public class TryFindLandNearWater {
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
                                if (param5x.getFluidState(param6.blockPosition()).is(FluidTags.WATER)) {
                                    return false;
                                } else if (param7 < var0.getValue()) {
                                    var0.setValue(param7 + 40L);
                                    return true;
                                } else {
                                    CollisionContext var0x = CollisionContext.of(param6);
                                    BlockPos var1x = param6.blockPosition();
                                    BlockPos.MutableBlockPos var2x = new BlockPos.MutableBlockPos();
            
                                    label45:
                                    for(BlockPos var3x : BlockPos.withinManhattan(var1x, param0, param0, param0)) {
                                        if ((var3x.getX() != var1x.getX() || var3x.getZ() != var1x.getZ())
                                            && param5x.getBlockState(var3x).getCollisionShape(param5x, var3x, var0x).isEmpty()
                                            && !param5x.getBlockState(var2x.setWithOffset(var3x, Direction.DOWN))
                                                .getCollisionShape(param5x, var3x, var0x)
                                                .isEmpty()) {
                                            for(Direction var4x : Direction.Plane.HORIZONTAL) {
                                                var2x.setWithOffset(var3x, var4x);
                                                if (param5x.getBlockState(var2x).isAir() && param5x.getBlockState(var2x.move(Direction.DOWN)).is(Blocks.WATER)) {
                                                    param5.set(new BlockPosTracker(var3x));
                                                    param4.set(new WalkTarget(new BlockPosTracker(var3x), param1, 0));
                                                    break label45;
                                                }
                                            }
                                        }
                                    }
            
                                    var0.setValue(param7 + 40L);
                                    return true;
                                }
                            }
                    )
        );
    }
}

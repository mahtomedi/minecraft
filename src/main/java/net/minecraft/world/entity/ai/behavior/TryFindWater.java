package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.mutable.MutableLong;

public class TryFindWater {
    public static BehaviorControl<PathfinderMob> create(int param0, float param1) {
        MutableLong var0 = new MutableLong(0L);
        return BehaviorBuilder.create(
            param3 -> param3.<MemoryAccessor, MemoryAccessor, MemoryAccessor>group(
                        param3.absent(MemoryModuleType.ATTACK_TARGET),
                        param3.absent(MemoryModuleType.WALK_TARGET),
                        param3.registered(MemoryModuleType.LOOK_TARGET)
                    )
                    .apply(param3, (param3x, param4, param5) -> (param5x, param6, param7) -> {
                            if (param5x.getFluidState(param6.blockPosition()).is(FluidTags.WATER)) {
                                return false;
                            } else if (param7 < var0.getValue()) {
                                var0.setValue(param7 + 20L + 2L);
                                return true;
                            } else {
                                BlockPos var0x = null;
                                BlockPos var1x = null;
                                BlockPos var2x = param6.blockPosition();
        
                                for(BlockPos var4x : BlockPos.withinManhattan(var2x, param0, param0, param0)) {
                                    if (var4x.getX() != var2x.getX() || var4x.getZ() != var2x.getZ()) {
                                        BlockState var5x = param6.level.getBlockState(var4x.above());
                                        BlockState var6 = param6.level.getBlockState(var4x);
                                        if (var6.is(Blocks.WATER)) {
                                            if (var5x.isAir()) {
                                                var0x = var4x.immutable();
                                                break;
                                            }
        
                                            if (var1x == null && !var4x.closerToCenterThan(param6.position(), 1.5)) {
                                                var1x = var4x.immutable();
                                            }
                                        }
                                    }
                                }
        
                                if (var0x == null) {
                                    var0x = var1x;
                                }
        
                                if (var0x != null) {
                                    param5.set(new BlockPosTracker(var0x));
                                    param4.set(new WalkTarget(new BlockPosTracker(var0x), param1, 0));
                                }
        
                                var0.setValue(param7 + 40L);
                                return true;
                            }
                        })
        );
    }
}

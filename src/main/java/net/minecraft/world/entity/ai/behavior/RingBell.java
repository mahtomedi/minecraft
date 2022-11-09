package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class RingBell {
    private static final float BELL_RING_CHANCE = 0.95F;
    public static final int RING_BELL_FROM_DISTANCE = 3;

    public static BehaviorControl<LivingEntity> create() {
        return BehaviorBuilder.create(
            param0 -> param0.<MemoryAccessor>group(param0.present(MemoryModuleType.MEETING_POINT)).apply(param0, param1 -> (param2, param3, param4) -> {
                        if (param2.random.nextFloat() <= 0.95F) {
                            return false;
                        } else {
                            BlockPos var0x = param0.<GlobalPos>get(param1).pos();
                            if (var0x.closerThan(param3.blockPosition(), 3.0)) {
                                BlockState var1x = param2.getBlockState(var0x);
                                if (var1x.is(Blocks.BELL)) {
                                    BellBlock var2 = (BellBlock)var1x.getBlock();
                                    var2.attemptToRing(param3, param2, var0x, null);
                                }
                            }
    
                            return true;
                        }
                    })
        );
    }
}

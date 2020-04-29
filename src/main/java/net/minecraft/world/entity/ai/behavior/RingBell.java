package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class RingBell extends Behavior<LivingEntity> {
    public RingBell() {
        super(ImmutableMap.of(MemoryModuleType.MEETING_POINT, MemoryStatus.VALUE_PRESENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel param0, LivingEntity param1) {
        return param0.random.nextFloat() > 0.95F;
    }

    @Override
    protected void start(ServerLevel param0, LivingEntity param1, long param2) {
        Brain<?> var0 = param1.getBrain();
        BlockPos var1 = var0.getMemory(MemoryModuleType.MEETING_POINT).get().pos();
        if (var1.closerThan(param1.blockPosition(), 3.0)) {
            BlockState var2 = param0.getBlockState(var1);
            if (var2.is(Blocks.BELL)) {
                BellBlock var3 = (BellBlock)var2.getBlock();
                var3.attemptToRing(param0, var1, null);
            }
        }

    }
}

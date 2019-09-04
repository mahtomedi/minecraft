package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

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
        if (var1.closerThan(new BlockPos(param1), 3.0)) {
            BlockState var2 = param0.getBlockState(var1);
            if (var2.getBlock() == Blocks.BELL) {
                BellBlock var3 = (BellBlock)var2.getBlock();

                for(Direction var4 : Direction.Plane.HORIZONTAL) {
                    if (var3.onHit(param0, var2, new BlockHitResult(new Vec3(0.5, 0.5, 0.5), var4, var1, false), null, false)) {
                        break;
                    }
                }
            }
        }

    }
}

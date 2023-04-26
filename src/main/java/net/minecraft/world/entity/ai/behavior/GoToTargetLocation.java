package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class GoToTargetLocation {
    private static BlockPos getNearbyPos(Mob param0, BlockPos param1) {
        RandomSource var0 = param0.level().random;
        return param1.offset(getRandomOffset(var0), 0, getRandomOffset(var0));
    }

    private static int getRandomOffset(RandomSource param0) {
        return param0.nextInt(3) - 1;
    }

    public static <E extends Mob> OneShot<E> create(MemoryModuleType<BlockPos> param0, int param1, float param2) {
        return BehaviorBuilder.create(
            param3 -> param3.<MemoryAccessor, MemoryAccessor, MemoryAccessor, MemoryAccessor>group(
                        param3.present(param0),
                        param3.absent(MemoryModuleType.ATTACK_TARGET),
                        param3.absent(MemoryModuleType.WALK_TARGET),
                        param3.registered(MemoryModuleType.LOOK_TARGET)
                    )
                    .apply(param3, (param3x, param4, param5, param6) -> (param4x, param5x, param6x) -> {
                            BlockPos var0x = param3.get(param3x);
                            boolean var1x = var0x.closerThan(param5x.blockPosition(), (double)param1);
                            if (!var1x) {
                                BehaviorUtils.setWalkAndLookTargetMemories(param5x, getNearbyPos(param5x, var0x), param2, param1);
                            }
        
                            return true;
                        })
        );
    }
}

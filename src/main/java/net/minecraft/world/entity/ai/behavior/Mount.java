package net.minecraft.world.entity.ai.behavior;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class Mount {
    private static final int CLOSE_ENOUGH_TO_START_RIDING_DIST = 1;

    public static BehaviorControl<LivingEntity> create(float param0) {
        return BehaviorBuilder.create(
            param1 -> param1.<MemoryAccessor, MemoryAccessor, MemoryAccessor>group(
                        param1.registered(MemoryModuleType.LOOK_TARGET),
                        param1.absent(MemoryModuleType.WALK_TARGET),
                        param1.present(MemoryModuleType.RIDE_TARGET)
                    )
                    .apply(param1, (param2, param3, param4) -> (param5, param6, param7) -> {
                            if (param6.isPassenger()) {
                                return false;
                            } else {
                                Entity var0x = param1.get(param4);
                                if (var0x.closerThan(param6, 1.0)) {
                                    param6.startRiding(var0x);
                                } else {
                                    param2.set(new EntityTracker(var0x, true));
                                    param3.set(new WalkTarget(new EntityTracker(var0x, false), param0, 1));
                                }
        
                                return true;
                            }
                        })
        );
    }
}

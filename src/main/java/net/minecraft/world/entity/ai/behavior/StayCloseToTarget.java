package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import java.util.function.Function;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class StayCloseToTarget {
    public static BehaviorControl<LivingEntity> create(Function<LivingEntity, Optional<PositionTracker>> param0, int param1, int param2, float param3) {
        return BehaviorBuilder.create(
            param4 -> param4.<MemoryAccessor, MemoryAccessor>group(param4.registered(MemoryModuleType.LOOK_TARGET), param4.absent(MemoryModuleType.WALK_TARGET))
                    .apply(param4, (param4x, param5) -> (param6, param7, param8) -> {
                            Optional<PositionTracker> var0x = param0.apply(param7);
                            if (var0x.isEmpty()) {
                                return false;
                            } else {
                                PositionTracker var1x = (PositionTracker)var0x.get();
                                if (param7.position().closerThan(var1x.currentPosition(), (double)param2)) {
                                    return false;
                                } else {
                                    PositionTracker var2x = (PositionTracker)var0x.get();
                                    param4x.set(var2x);
                                    param5.set(new WalkTarget(var2x, param3, param1));
                                    return true;
                                }
                            }
                        })
        );
    }
}

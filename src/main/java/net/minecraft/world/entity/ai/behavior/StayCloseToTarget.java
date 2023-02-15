package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class StayCloseToTarget {
    public static BehaviorControl<LivingEntity> create(
        Function<LivingEntity, Optional<PositionTracker>> param0, Predicate<LivingEntity> param1, int param2, int param3, float param4
    ) {
        return BehaviorBuilder.create(
            param5 -> param5.<MemoryAccessor, MemoryAccessor>group(
                        param5.registered(MemoryModuleType.LOOK_TARGET), param5.registered(MemoryModuleType.WALK_TARGET)
                    )
                    .apply(param5, (param5x, param6) -> (param7, param8, param9) -> {
                            Optional<PositionTracker> var0x = param0.apply(param8);
                            if (!var0x.isEmpty() && param1.test(param8)) {
                                PositionTracker var1x = (PositionTracker)var0x.get();
                                if (param8.position().closerThan(var1x.currentPosition(), (double)param3)) {
                                    return false;
                                } else {
                                    PositionTracker var2x = (PositionTracker)var0x.get();
                                    param5x.set(var2x);
                                    param6.set(new WalkTarget(var2x, param4, param2));
                                    return true;
                                }
                            } else {
                                return false;
                            }
                        })
        );
    }
}

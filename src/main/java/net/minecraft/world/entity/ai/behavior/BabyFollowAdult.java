package net.minecraft.world.entity.ai.behavior;

import java.util.function.Function;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class BabyFollowAdult {
    public static OneShot<AgeableMob> create(UniformInt param0, float param1) {
        return create(param0, param1x -> param1);
    }

    public static OneShot<AgeableMob> create(UniformInt param0, Function<LivingEntity, Float> param1) {
        return BehaviorBuilder.create(
            param2 -> param2.<MemoryAccessor, MemoryAccessor, MemoryAccessor>group(
                        param2.present(MemoryModuleType.NEAREST_VISIBLE_ADULT),
                        param2.registered(MemoryModuleType.LOOK_TARGET),
                        param2.absent(MemoryModuleType.WALK_TARGET)
                    )
                    .apply(param2, (param3, param4, param5) -> (param6, param7, param8) -> {
                            if (!param7.isBaby()) {
                                return false;
                            } else {
                                AgeableMob var0x = param2.get(param3);
                                if (param7.closerThan(var0x, (double)(param0.getMaxValue() + 1)) && !param7.closerThan(var0x, (double)param0.getMinValue())) {
                                    WalkTarget var1x = new WalkTarget(new EntityTracker(var0x, false), param1.apply(param7), param0.getMinValue() - 1);
                                    param4.set(new EntityTracker(var0x, true));
                                    param5.set(var1x);
                                    return true;
                                } else {
                                    return false;
                                }
                            }
                        })
        );
    }
}

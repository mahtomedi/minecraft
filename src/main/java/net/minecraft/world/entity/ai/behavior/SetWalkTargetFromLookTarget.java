package net.minecraft.world.entity.ai.behavior;

import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class SetWalkTargetFromLookTarget {
    public static OneShot<LivingEntity> create(float param0, int param1) {
        return create(param0x -> true, param1x -> param0, param1);
    }

    public static OneShot<LivingEntity> create(Predicate<LivingEntity> param0, Function<LivingEntity, Float> param1, int param2) {
        return BehaviorBuilder.create(
            param3 -> param3.<MemoryAccessor, MemoryAccessor>group(param3.absent(MemoryModuleType.WALK_TARGET), param3.present(MemoryModuleType.LOOK_TARGET))
                    .apply(param3, (param4, param5) -> (param6, param7, param8) -> {
                            if (!param0.test(param7)) {
                                return false;
                            } else {
                                param4.set(new WalkTarget(param3.get(param5), param1.apply(param7), param2));
                                return true;
                            }
                        })
        );
    }
}

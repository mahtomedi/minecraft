package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

public class SetEntityLookTarget {
    public static BehaviorControl<LivingEntity> create(MobCategory param0, float param1) {
        return create(param1x -> param0.equals(param1x.getType().getCategory()), param1);
    }

    public static OneShot<LivingEntity> create(EntityType<?> param0, float param1) {
        return create(param1x -> param0.equals(param1x.getType()), param1);
    }

    public static OneShot<LivingEntity> create(float param0) {
        return create(param0x -> true, param0);
    }

    public static OneShot<LivingEntity> create(Predicate<LivingEntity> param0, float param1) {
        float var0 = param1 * param1;
        return BehaviorBuilder.create(
            param2 -> param2.<MemoryAccessor, MemoryAccessor>group(
                        param2.absent(MemoryModuleType.LOOK_TARGET), param2.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
                    )
                    .apply(
                        param2,
                        (param3, param4) -> (param5, param6, param7) -> {
                                Optional<LivingEntity> var0x = param2.<NearestVisibleLivingEntities>get(param4)
                                    .findClosest(param0.and(param2x -> param2x.distanceToSqr(param6) <= (double)var0 && !param6.hasPassenger(param2x)));
                                if (var0x.isEmpty()) {
                                    return false;
                                } else {
                                    param3.set(new EntityTracker(var0x.get(), true));
                                    return true;
                                }
                            }
                    )
        );
    }
}

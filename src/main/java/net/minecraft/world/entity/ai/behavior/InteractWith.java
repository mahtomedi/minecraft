package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class InteractWith {
    public static <T extends LivingEntity> BehaviorControl<LivingEntity> of(
        EntityType<? extends T> param0, int param1, MemoryModuleType<T> param2, float param3, int param4
    ) {
        return of(param0, param1, param0x -> true, param0x -> true, param2, param3, param4);
    }

    public static <E extends LivingEntity, T extends LivingEntity> BehaviorControl<E> of(
        EntityType<? extends T> param0, int param1, Predicate<E> param2, Predicate<T> param3, MemoryModuleType<T> param4, float param5, int param6
    ) {
        int var0 = param1 * param1;
        Predicate<LivingEntity> var1 = param2x -> param0.equals(param2x.getType()) && param3.test((T)param2x);
        return BehaviorBuilder.create(
            param6x -> param6x.<MemoryAccessor, MemoryAccessor, MemoryAccessor, MemoryAccessor>group(
                        param6x.registered(param4),
                        param6x.registered(MemoryModuleType.LOOK_TARGET),
                        param6x.absent(MemoryModuleType.WALK_TARGET),
                        param6x.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
                    )
                    .apply(param6x, (param6xx, param7, param8, param9) -> (param10, param11, param12) -> {
                            NearestVisibleLivingEntities var0x = param6x.get(param9);
                            if (param2.test(param11) && var0x.contains(var1)) {
                                Optional<LivingEntity> var1x = var0x.findClosest(
                                    param3x -> param3x.distanceToSqr(param11) <= (double)var0 && var1.test(param3x)
                                );
                                var1x.ifPresent(param5x -> {
                                    param6xx.set(param5x);
                                    param7.set(new EntityTracker(param5x, true));
                                    param8.set(new WalkTarget(new EntityTracker(param5x, false), param5, param6));
                                });
                                return true;
                            } else {
                                return false;
                            }
                        })
        );
    }
}

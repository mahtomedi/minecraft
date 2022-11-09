package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

public class SetLookAndInteract {
    public static BehaviorControl<LivingEntity> create(EntityType<?> param0, int param1) {
        int var0 = param1 * param1;
        return BehaviorBuilder.create(
            param2 -> param2.<MemoryAccessor, MemoryAccessor, MemoryAccessor>group(
                        param2.registered(MemoryModuleType.LOOK_TARGET),
                        param2.absent(MemoryModuleType.INTERACTION_TARGET),
                        param2.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
                    )
                    .apply(
                        param2,
                        (param3, param4, param5) -> (param6, param7, param8) -> {
                                Optional<LivingEntity> var0x = param2.<NearestVisibleLivingEntities>get(param5)
                                    .findClosest(param3x -> param3x.distanceToSqr(param7) <= (double)var0 && param0.equals(param3x.getType()));
                                if (var0x.isEmpty()) {
                                    return false;
                                } else {
                                    LivingEntity var1x = var0x.get();
                                    param4.set(var1x);
                                    param3.set(new EntityTracker(var1x, true));
                                    return true;
                                }
                            }
                    )
        );
    }
}

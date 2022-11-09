package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import java.util.function.Function;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class SetWalkTargetFromAttackTargetIfTargetOutOfReach {
    private static final int PROJECTILE_ATTACK_RANGE_BUFFER = 1;

    public static BehaviorControl<Mob> create(float param0) {
        return create(param1 -> param0);
    }

    public static BehaviorControl<Mob> create(Function<LivingEntity, Float> param0) {
        return BehaviorBuilder.create(
            param1 -> param1.<MemoryAccessor, MemoryAccessor, MemoryAccessor, MemoryAccessor>group(
                        param1.registered(MemoryModuleType.WALK_TARGET),
                        param1.registered(MemoryModuleType.LOOK_TARGET),
                        param1.present(MemoryModuleType.ATTACK_TARGET),
                        param1.registered(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
                    )
                    .apply(
                        param1,
                        (param2, param3, param4, param5) -> (param6, param7, param8) -> {
                                LivingEntity var0x = param1.get(param4);
                                Optional<NearestVisibleLivingEntities> var1x = param1.tryGet(param5);
                                if (var1x.isPresent()
                                    && ((NearestVisibleLivingEntities)var1x.get()).contains(var0x)
                                    && BehaviorUtils.isWithinAttackRange(param7, var0x, 1)) {
                                    param2.erase();
                                } else {
                                    param3.set(new EntityTracker(var0x, true));
                                    param2.set(new WalkTarget(new EntityTracker(var0x, false), param0.apply(param7), 0));
                                }
            
                                return true;
                            }
                    )
        );
    }
}

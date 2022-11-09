package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class SocializeAtBell {
    private static final float SPEED_MODIFIER = 0.3F;

    public static OneShot<LivingEntity> create() {
        return BehaviorBuilder.create(
            param0 -> param0.<MemoryAccessor, MemoryAccessor, MemoryAccessor, MemoryAccessor, MemoryAccessor>group(
                        param0.registered(MemoryModuleType.WALK_TARGET),
                        param0.registered(MemoryModuleType.LOOK_TARGET),
                        param0.present(MemoryModuleType.MEETING_POINT),
                        param0.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES),
                        param0.absent(MemoryModuleType.INTERACTION_TARGET)
                    )
                    .apply(
                        param0,
                        (param1, param2, param3, param4, param5) -> (param6, param7, param8) -> {
                                GlobalPos var0x = param0.get(param3);
                                NearestVisibleLivingEntities var1x = param0.get(param4);
                                if (param6.getRandom().nextInt(100) == 0
                                    && param6.dimension() == var0x.dimension()
                                    && var0x.pos().closerToCenterThan(param7.position(), 4.0)
                                    && var1x.contains(param0x -> EntityType.VILLAGER.equals(param0x.getType()))) {
                                    var1x.findClosest(param1x -> EntityType.VILLAGER.equals(param1x.getType()) && param1x.distanceToSqr(param7) <= 32.0)
                                        .ifPresent(param3x -> {
                                            param5.set(param3x);
                                            param2.set(new EntityTracker(param3x, true));
                                            param1.set(new WalkTarget(new EntityTracker(param3x, false), 0.3F, 1));
                                        });
                                    return true;
                                } else {
                                    return false;
                                }
                            }
                    )
        );
    }
}

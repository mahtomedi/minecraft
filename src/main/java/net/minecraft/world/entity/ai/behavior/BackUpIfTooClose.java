package net.minecraft.world.entity.ai.behavior;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

public class BackUpIfTooClose {
    public static OneShot<Mob> create(int param0, float param1) {
        return BehaviorBuilder.create(
            param2 -> param2.<MemoryAccessor, MemoryAccessor, MemoryAccessor, MemoryAccessor>group(
                        param2.absent(MemoryModuleType.WALK_TARGET),
                        param2.registered(MemoryModuleType.LOOK_TARGET),
                        param2.present(MemoryModuleType.ATTACK_TARGET),
                        param2.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
                    )
                    .apply(param2, (param3, param4, param5, param6) -> (param6x, param7, param8) -> {
                            LivingEntity var0x = param2.get(param5);
                            if (var0x.closerThan(param7, (double)param0) && param2.<NearestVisibleLivingEntities>get(param6).contains(var0x)) {
                                param4.set(new EntityTracker(var0x, true));
                                param7.getMoveControl().strafe(-param1, 0.0F);
                                param7.setYRot(Mth.rotateIfNecessary(param7.getYRot(), param7.yHeadRot, 0.0F));
                                return true;
                            } else {
                                return false;
                            }
                        })
        );
    }
}

package net.minecraft.world.entity.ai.behavior.warden;

import java.util.Optional;
import java.util.function.Function;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.warden.Warden;

public class SetRoarTarget {
    public static <E extends Warden> BehaviorControl<E> create(Function<E, Optional<? extends LivingEntity>> param0) {
        return BehaviorBuilder.create(
            param1 -> param1.<MemoryAccessor, MemoryAccessor, MemoryAccessor>group(
                        param1.absent(MemoryModuleType.ROAR_TARGET),
                        param1.absent(MemoryModuleType.ATTACK_TARGET),
                        param1.registered(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)
                    )
                    .apply(param1, (param1x, param2, param3) -> (param3x, param4, param5) -> {
                            Optional<? extends LivingEntity> var0x = param0.apply(param4);
                            if (var0x.filter(param4::canTargetEntity).isEmpty()) {
                                return false;
                            } else {
                                param1x.set((LivingEntity)var0x.get());
                                param3.erase();
                                return true;
                            }
                        })
        );
    }
}

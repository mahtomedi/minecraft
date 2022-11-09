package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.util.Function3;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.behavior.declarative.Trigger;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class BecomePassiveIfMemoryPresent {
    public static BehaviorControl<LivingEntity> create(MemoryModuleType<?> param0, int param1) {
        return BehaviorBuilder.create(
            param2 -> param2.group(param2.registered(MemoryModuleType.ATTACK_TARGET), param2.absent(MemoryModuleType.PACIFIED), param2.present(param0))
                    .apply(
                        param2,
                        param2.point(
                            () -> "[BecomePassive if " + param0 + " present]",
                            (Function3<MemoryAccessor, MemoryAccessor, MemoryAccessor, Trigger<LivingEntity>>)(param1x, param2x, param3) -> (param3x, param4, param5) -> {
                                    param2x.setWithExpiry(true, (long)param1);
                                    param1x.erase();
                                    return true;
                                }
                        )
                    )
        );
    }
}

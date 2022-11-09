package net.minecraft.world.entity.monster.piglin;

import java.util.Optional;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;

public class StopAdmiringIfItemTooFarAway<E extends Piglin> {
    public static BehaviorControl<LivingEntity> create(int param0) {
        return BehaviorBuilder.create(
            param1 -> param1.<MemoryAccessor, MemoryAccessor>group(
                        param1.present(MemoryModuleType.ADMIRING_ITEM), param1.registered(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM)
                    )
                    .apply(param1, (param2, param3) -> (param4, param5, param6) -> {
                            if (!param5.getOffhandItem().isEmpty()) {
                                return false;
                            } else {
                                Optional<ItemEntity> var0x = param1.tryGet(param3);
                                if (var0x.isPresent() && ((ItemEntity)var0x.get()).closerThan(param5, (double)param0)) {
                                    return false;
                                } else {
                                    param2.erase();
                                    return true;
                                }
                            }
                        })
        );
    }
}

package net.minecraft.world.entity.monster.piglin;

import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.item.Items;

public class StopHoldingItemIfNoLongerAdmiring {
    public static BehaviorControl<Piglin> create() {
        return BehaviorBuilder.create(
            param0 -> param0.<MemoryAccessor>group(param0.absent(MemoryModuleType.ADMIRING_ITEM)).apply(param0, param0x -> (param0xx, param1, param2) -> {
                        if (!param1.getOffhandItem().isEmpty() && !param1.getOffhandItem().is(Items.SHIELD)) {
                            PiglinAi.stopHoldingOffHandItem(param1, true);
                            return true;
                        } else {
                            return false;
                        }
                    })
        );
    }
}

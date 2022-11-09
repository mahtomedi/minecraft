package net.minecraft.world.entity.monster.piglin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;

public class StartAdmiringItemIfSeen {
    public static BehaviorControl<LivingEntity> create(int param0) {
        return BehaviorBuilder.create(
            param1 -> param1.<MemoryAccessor, MemoryAccessor, MemoryAccessor, MemoryAccessor>group(
                        param1.present(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM),
                        param1.absent(MemoryModuleType.ADMIRING_ITEM),
                        param1.absent(MemoryModuleType.ADMIRING_DISABLED),
                        param1.absent(MemoryModuleType.DISABLE_WALK_TO_ADMIRE_ITEM)
                    )
                    .apply(param1, (param2, param3, param4, param5) -> (param4x, param5x, param6) -> {
                            ItemEntity var0x = param1.get(param2);
                            if (!PiglinAi.isLovedItem(var0x.getItem())) {
                                return false;
                            } else {
                                param3.setWithExpiry(true, (long)param0);
                                return true;
                            }
                        })
        );
    }
}

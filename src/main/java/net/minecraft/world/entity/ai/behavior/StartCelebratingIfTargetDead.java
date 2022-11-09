package net.minecraft.world.entity.ai.behavior;

import java.util.function.BiPredicate;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.GameRules;

public class StartCelebratingIfTargetDead {
    public static BehaviorControl<LivingEntity> create(int param0, BiPredicate<LivingEntity, LivingEntity> param1) {
        return BehaviorBuilder.create(
            param2 -> param2.<MemoryAccessor, MemoryAccessor, MemoryAccessor, MemoryAccessor>group(
                        param2.present(MemoryModuleType.ATTACK_TARGET),
                        param2.registered(MemoryModuleType.ANGRY_AT),
                        param2.absent(MemoryModuleType.CELEBRATE_LOCATION),
                        param2.registered(MemoryModuleType.DANCING)
                    )
                    .apply(param2, (param3, param4, param5, param6) -> (param7, param8, param9) -> {
                            LivingEntity var0x = param2.get(param3);
                            if (!var0x.isDeadOrDying()) {
                                return false;
                            } else {
                                if (param1.test(param8, var0x)) {
                                    param6.setWithExpiry(true, (long)param0);
                                }
        
                                param5.setWithExpiry(var0x.blockPosition(), (long)param0);
                                if (var0x.getType() != EntityType.PLAYER || param7.getGameRules().getBoolean(GameRules.RULE_FORGIVE_DEAD_PLAYERS)) {
                                    param3.erase();
                                    param4.erase();
                                }
        
                                return true;
                            }
                        })
        );
    }
}

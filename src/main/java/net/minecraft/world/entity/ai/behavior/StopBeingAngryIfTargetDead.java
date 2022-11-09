package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.GameRules;

public class StopBeingAngryIfTargetDead {
    public static BehaviorControl<LivingEntity> create() {
        return BehaviorBuilder.create(
            param0 -> param0.<MemoryAccessor>group(param0.present(MemoryModuleType.ANGRY_AT))
                    .apply(
                        param0,
                        param1 -> (param2, param3, param4) -> {
                                Optional.ofNullable(param2.getEntity(param0.get(param1)))
                                    .map(param0x -> param0x instanceof LivingEntity var0x ? var0x : null)
                                    .filter(LivingEntity::isDeadOrDying)
                                    .filter(
                                        param1x -> param1x.getType() != EntityType.PLAYER
                                                || param2.getGameRules().getBoolean(GameRules.RULE_FORGIVE_DEAD_PLAYERS)
                                    )
                                    .ifPresent(param1x -> param1.erase());
                                return true;
                            }
                    )
        );
    }
}

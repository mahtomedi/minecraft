package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.function.BiPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.GameRules;

public class StartCelebratingIfTargetDead extends Behavior<LivingEntity> {
    private final int celebrateDuration;
    private final BiPredicate<LivingEntity, LivingEntity> dancePredicate;

    public StartCelebratingIfTargetDead(int param0, BiPredicate<LivingEntity, LivingEntity> param1) {
        super(
            ImmutableMap.of(
                MemoryModuleType.ATTACK_TARGET,
                MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.ANGRY_AT,
                MemoryStatus.REGISTERED,
                MemoryModuleType.CELEBRATE_LOCATION,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.DANCING,
                MemoryStatus.REGISTERED
            )
        );
        this.celebrateDuration = param0;
        this.dancePredicate = param1;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel param0, LivingEntity param1) {
        return this.getAttackTarget(param1).isDeadOrDying();
    }

    @Override
    protected void start(ServerLevel param0, LivingEntity param1, long param2) {
        LivingEntity var0 = this.getAttackTarget(param1);
        if (this.dancePredicate.test(param1, var0)) {
            param1.getBrain().setMemoryWithExpiry(MemoryModuleType.DANCING, true, (long)this.celebrateDuration);
        }

        param1.getBrain().setMemoryWithExpiry(MemoryModuleType.CELEBRATE_LOCATION, var0.blockPosition(), (long)this.celebrateDuration);
        if (var0.getType() != EntityType.PLAYER || param0.getGameRules().getBoolean(GameRules.RULE_FORGIVE_DEAD_PLAYERS)) {
            param1.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
            param1.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
        }

    }

    private LivingEntity getAttackTarget(LivingEntity param0) {
        return param0.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
    }
}

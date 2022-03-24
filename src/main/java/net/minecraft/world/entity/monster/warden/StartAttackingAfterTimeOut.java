package net.minecraft.world.entity.monster.warden;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class StartAttackingAfterTimeOut extends StartAttacking<Warden> {
    public StartAttackingAfterTimeOut(Predicate<Warden> param0, Function<Warden, Optional<? extends LivingEntity>> param1, int param2) {
        super(param0, param1, param2);
    }

    protected void start(ServerLevel param0, Warden param1, long param2) {
        BehaviorUtils.lookAtEntity(param1, param1.getBrain().getMemory(MemoryModuleType.ROAR_TARGET).get());
    }

    protected void stop(ServerLevel param0, Warden param1, long param2) {
        this.startAttacking(param0, param1, param2);
    }

    private void startAttacking(ServerLevel param0, Warden param1, long param2) {
        super.start(param0, param1, param2);
        param1.getBrain().eraseMemory(MemoryModuleType.ROAR_TARGET);
    }

    protected boolean canStillUse(ServerLevel param0, Warden param1, long param2) {
        Optional<LivingEntity> var0 = param1.getBrain().getMemory(MemoryModuleType.ROAR_TARGET);
        return var0.filter(EntitySelector.NO_CREATIVE_OR_SPECTATOR).isPresent();
    }
}
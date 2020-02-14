package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.function.BiPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class DismountOrSkipMounting<E extends LivingEntity, T extends Entity> extends Behavior<E> {
    private final int maxWalkDistToRideTarget;
    private final BiPredicate<E, Entity> dontRideIf;

    public DismountOrSkipMounting(int param0, BiPredicate<E, Entity> param1) {
        super(ImmutableMap.of(MemoryModuleType.RIDE_TARGET, MemoryStatus.REGISTERED));
        this.maxWalkDistToRideTarget = param0;
        this.dontRideIf = param1;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel param0, E param1) {
        Entity var0 = param1.getVehicle();
        Entity var1 = param1.getBrain().getMemory(MemoryModuleType.RIDE_TARGET).orElse(null);
        if (var0 == null && var1 == null) {
            return false;
        } else {
            Entity var2 = var0 == null ? var1 : var0;
            return !this.isVehicleValid(param1, var2) || this.dontRideIf.test(param1, var2);
        }
    }

    private boolean isVehicleValid(E param0, Entity param1) {
        return param1.isAlive() && param1.closerThan(param0, (double)this.maxWalkDistToRideTarget) && param1.level == param0.level;
    }

    @Override
    protected void start(ServerLevel param0, E param1, long param2) {
        param1.stopRiding();
        param1.getBrain().eraseMemory(MemoryModuleType.RIDE_TARGET);
    }
}

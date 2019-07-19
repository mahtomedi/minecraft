package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.schedule.Activity;

public class WakeUp extends Behavior<LivingEntity> {
    public WakeUp() {
        super(ImmutableMap.of());
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel param0, LivingEntity param1) {
        return !param1.getBrain().isActive(Activity.REST) && param1.isSleeping();
    }

    @Override
    protected void start(ServerLevel param0, LivingEntity param1, long param2) {
        param1.stopSleeping();
    }
}

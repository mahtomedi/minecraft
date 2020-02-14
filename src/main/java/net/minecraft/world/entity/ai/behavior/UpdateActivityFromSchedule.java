package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public class UpdateActivityFromSchedule extends Behavior<LivingEntity> {
    public UpdateActivityFromSchedule() {
        super(ImmutableMap.of());
    }

    @Override
    protected void start(ServerLevel param0, LivingEntity param1, long param2) {
        param1.getBrain().updateActivityFromSchedule(param0.getDayTime(), param0.getGameTime());
    }
}

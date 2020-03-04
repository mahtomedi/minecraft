package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.schedule.Activity;

public class ResetRaidStatus extends Behavior<LivingEntity> {
    public ResetRaidStatus() {
        super(ImmutableMap.of());
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel param0, LivingEntity param1) {
        return param0.random.nextInt(20) == 0;
    }

    @Override
    protected void start(ServerLevel param0, LivingEntity param1, long param2) {
        Brain<?> var0 = param1.getBrain();
        Raid var1 = param0.getRaidAt(param1.blockPosition());
        if (var1 == null || var1.isStopped() || var1.isLoss()) {
            var0.setDefaultActivity(Activity.IDLE);
            var0.updateActivityFromSchedule(param0.getDayTime(), param0.getGameTime());
        }

    }
}

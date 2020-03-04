package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.schedule.Activity;

public class SetRaidStatus extends Behavior<LivingEntity> {
    public SetRaidStatus() {
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
        if (var1 != null) {
            if (var1.hasFirstWaveSpawned() && !var1.isBetweenWaves()) {
                var0.setDefaultActivity(Activity.RAID);
                var0.setActiveActivityIfPossible(Activity.RAID);
            } else {
                var0.setDefaultActivity(Activity.PRE_RAID);
                var0.setActiveActivityIfPossible(Activity.PRE_RAID);
            }
        }

    }
}

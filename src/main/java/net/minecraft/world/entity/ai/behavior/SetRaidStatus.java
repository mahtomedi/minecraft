package net.minecraft.world.entity.ai.behavior;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.schedule.Activity;

public class SetRaidStatus {
    public static BehaviorControl<LivingEntity> create() {
        return BehaviorBuilder.create(param0 -> param0.point((param0x, param1, param2) -> {
                if (param0x.random.nextInt(20) != 0) {
                    return false;
                } else {
                    Brain<?> var0x = param1.getBrain();
                    Raid var1 = param0x.getRaidAt(param1.blockPosition());
                    if (var1 != null) {
                        if (var1.hasFirstWaveSpawned() && !var1.isBetweenWaves()) {
                            var0x.setDefaultActivity(Activity.RAID);
                            var0x.setActiveActivityIfPossible(Activity.RAID);
                        } else {
                            var0x.setDefaultActivity(Activity.PRE_RAID);
                            var0x.setActiveActivityIfPossible(Activity.PRE_RAID);
                        }
                    }

                    return true;
                }
            }));
    }
}

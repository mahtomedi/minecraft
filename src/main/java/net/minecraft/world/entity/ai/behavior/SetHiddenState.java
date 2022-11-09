package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.apache.commons.lang3.mutable.MutableInt;

public class SetHiddenState {
    private static final int HIDE_TIMEOUT = 300;

    public static BehaviorControl<LivingEntity> create(int param0, int param1) {
        int var0 = param0 * 20;
        MutableInt var1 = new MutableInt(0);
        return BehaviorBuilder.create(
            param3 -> param3.<MemoryAccessor, MemoryAccessor>group(
                        param3.present(MemoryModuleType.HIDING_PLACE), param3.present(MemoryModuleType.HEARD_BELL_TIME)
                    )
                    .apply(param3, (param4, param5) -> (param6, param7, param8) -> {
                            long var0x = param3.<Long>get(param5);
                            boolean var1x = var0x + 300L <= param8;
                            if (var1.getValue() <= var0 && !var1x) {
                                BlockPos var2x = param3.<GlobalPos>get(param4).pos();
                                if (var2x.closerThan(param7.blockPosition(), (double)param1)) {
                                    var1.increment();
                                }
        
                                return true;
                            } else {
                                param5.erase();
                                param4.erase();
                                param7.getBrain().updateActivityFromSchedule(param6.getDayTime(), param6.getGameTime());
                                var1.setValue(0);
                                return true;
                            }
                        })
        );
    }
}

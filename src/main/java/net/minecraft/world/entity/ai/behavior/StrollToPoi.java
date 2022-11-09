package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import org.apache.commons.lang3.mutable.MutableLong;

public class StrollToPoi {
    public static BehaviorControl<PathfinderMob> create(MemoryModuleType<GlobalPos> param0, float param1, int param2, int param3) {
        MutableLong var0 = new MutableLong(0L);
        return BehaviorBuilder.create(
            param5 -> param5.<MemoryAccessor, MemoryAccessor>group(param5.registered(MemoryModuleType.WALK_TARGET), param5.present(param0))
                    .apply(param5, (param5x, param6) -> (param7, param8, param9) -> {
                            GlobalPos var0x = param5.get(param6);
                            if (param7.dimension() != var0x.dimension() || !var0x.pos().closerToCenterThan(param8.position(), (double)param3)) {
                                return false;
                            } else if (param9 <= var0.getValue()) {
                                return true;
                            } else {
                                param5x.set(new WalkTarget(var0x.pos(), param1, param2));
                                var0.setValue(param9 + 80L);
                                return true;
                            }
                        })
        );
    }
}

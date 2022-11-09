package net.minecraft.world.entity.ai.behavior;

import java.util.List;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.Villager;
import org.apache.commons.lang3.mutable.MutableLong;

public class StrollToPoiList {
    public static BehaviorControl<Villager> create(
        MemoryModuleType<List<GlobalPos>> param0, float param1, int param2, int param3, MemoryModuleType<GlobalPos> param4
    ) {
        MutableLong var0 = new MutableLong(0L);
        return BehaviorBuilder.create(
            param6 -> param6.<MemoryAccessor, MemoryAccessor, MemoryAccessor>group(
                        param6.registered(MemoryModuleType.WALK_TARGET), param6.present(param0), param6.present(param4)
                    )
                    .apply(param6, (param5x, param6x, param7) -> (param8, param9, param10) -> {
                            List<GlobalPos> var0x = param6.get(param6x);
                            GlobalPos var1x = param6.get(param7);
                            if (var0x.isEmpty()) {
                                return false;
                            } else {
                                GlobalPos var2x = var0x.get(param8.getRandom().nextInt(var0x.size()));
                                if (var2x != null
                                    && param8.dimension() == var2x.dimension()
                                    && var1x.pos().closerToCenterThan(param9.position(), (double)param3)) {
                                    if (param10 > var0.getValue()) {
                                        param5x.set(new WalkTarget(var2x.pos(), param1, param2));
                                        var0.setValue(param10 + 100L);
                                    }
        
                                    return true;
                                } else {
                                    return false;
                                }
                            }
                        })
        );
    }
}

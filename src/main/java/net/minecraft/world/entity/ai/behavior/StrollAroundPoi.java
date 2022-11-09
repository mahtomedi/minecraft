package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableLong;

public class StrollAroundPoi {
    private static final int MIN_TIME_BETWEEN_STROLLS = 180;
    private static final int STROLL_MAX_XZ_DIST = 8;
    private static final int STROLL_MAX_Y_DIST = 6;

    public static OneShot<PathfinderMob> create(MemoryModuleType<GlobalPos> param0, float param1, int param2) {
        MutableLong var0 = new MutableLong(0L);
        return BehaviorBuilder.create(
            param4 -> param4.<MemoryAccessor, MemoryAccessor>group(param4.registered(MemoryModuleType.WALK_TARGET), param4.present(param0))
                    .apply(param4, (param4x, param5) -> (param6, param7, param8) -> {
                            GlobalPos var0x = param4.get(param5);
                            if (param6.dimension() != var0x.dimension() || !var0x.pos().closerToCenterThan(param7.position(), (double)param2)) {
                                return false;
                            } else if (param8 <= var0.getValue()) {
                                return true;
                            } else {
                                Optional<Vec3> var1x = Optional.ofNullable(LandRandomPos.getPos(param7, 8, 6));
                                param4x.setOrErase(var1x.map(param1x -> new WalkTarget(param1x, param1, 1)));
                                var0.setValue(param8 + 180L);
                                return true;
                            }
                        })
        );
    }
}

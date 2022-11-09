package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

public class VillageBoundRandomStroll {
    private static final int MAX_XZ_DIST = 10;
    private static final int MAX_Y_DIST = 7;

    public static OneShot<PathfinderMob> create(float param0) {
        return create(param0, 10, 7);
    }

    public static OneShot<PathfinderMob> create(float param0, int param1, int param2) {
        return BehaviorBuilder.create(
            param3 -> param3.<MemoryAccessor>group(param3.absent(MemoryModuleType.WALK_TARGET)).apply(param3, param3x -> (param4, param5, param6) -> {
                        BlockPos var0x = param5.blockPosition();
                        Vec3 var1;
                        if (param4.isVillage(var0x)) {
                            var1 = LandRandomPos.getPos(param5, param1, param2);
                        } else {
                            SectionPos var2x = SectionPos.of(var0x);
                            SectionPos var3x = BehaviorUtils.findSectionClosestToVillage(param4, var2x, 2);
                            if (var3x != var2x) {
                                var1 = DefaultRandomPos.getPosTowards(param5, param1, param2, Vec3.atBottomCenterOf(var3x.center()), (float) (Math.PI / 2));
                            } else {
                                var1 = LandRandomPos.getPos(param5, param1, param2);
                            }
                        }
    
                        param3x.setOrErase(Optional.ofNullable(var1).map(param1x -> new WalkTarget(param1x, param0, 0)));
                        return true;
                    })
        );
    }
}

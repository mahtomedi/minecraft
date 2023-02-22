package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.Vec3;

public class SetWalkTargetFromBlockMemory {
    public static OneShot<Villager> create(MemoryModuleType<GlobalPos> param0, float param1, int param2, int param3, int param4) {
        return BehaviorBuilder.create(
            param5 -> param5.<MemoryAccessor, MemoryAccessor, MemoryAccessor>group(
                        param5.registered(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE), param5.absent(MemoryModuleType.WALK_TARGET), param5.present(param0)
                    )
                    .apply(param5, (param6, param7, param8) -> (param9, param10, param11) -> {
                            GlobalPos var0x = param5.get(param8);
                            Optional<Long> var1x = param5.tryGet(param6);
                            if (var0x.dimension() == param9.dimension() && (!var1x.isPresent() || param9.getGameTime() - var1x.get() <= (long)param4)) {
                                if (var0x.pos().distManhattan(param10.blockPosition()) > param3) {
                                    Vec3 var2x = null;
                                    int var3x = 0;
                                    int var4x = 1000;
        
                                    while(var2x == null || BlockPos.containing(var2x).distManhattan(param10.blockPosition()) > param3) {
                                        var2x = DefaultRandomPos.getPosTowards(param10, 15, 7, Vec3.atBottomCenterOf(var0x.pos()), (float) (Math.PI / 2));
                                        if (++var3x == 1000) {
                                            param10.releasePoi(param0);
                                            param8.erase();
                                            param6.set(param11);
                                            return true;
                                        }
                                    }
        
                                    param7.set(new WalkTarget(var2x, param1, param2));
                                } else if (var0x.pos().distManhattan(param10.blockPosition()) > param2) {
                                    param7.set(new WalkTarget(var0x.pos(), param1, param2));
                                }
                            } else {
                                param10.releasePoi(param0);
                                param8.erase();
                                param6.set(param11);
                            }
        
                            return true;
                        })
        );
    }
}

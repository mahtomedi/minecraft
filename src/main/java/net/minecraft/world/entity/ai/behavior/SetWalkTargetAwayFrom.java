package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

public class SetWalkTargetAwayFrom {
    public static BehaviorControl<PathfinderMob> pos(MemoryModuleType<BlockPos> param0, float param1, int param2, boolean param3) {
        return create(param0, param1, param2, param3, Vec3::atBottomCenterOf);
    }

    public static OneShot<PathfinderMob> entity(MemoryModuleType<? extends Entity> param0, float param1, int param2, boolean param3) {
        return create(param0, param1, param2, param3, Entity::position);
    }

    private static <T> OneShot<PathfinderMob> create(MemoryModuleType<T> param0, float param1, int param2, boolean param3, Function<T, Vec3> param4) {
        return BehaviorBuilder.create(
            param5 -> param5.<MemoryAccessor, MemoryAccessor>group(param5.registered(MemoryModuleType.WALK_TARGET), param5.present(param0))
                    .apply(param5, (param5x, param6) -> (param7, param8, param9) -> {
                            Optional<WalkTarget> var0x = param5.tryGet(param5x);
                            if (var0x.isPresent() && !param3) {
                                return false;
                            } else {
                                Vec3 var1x = param8.position();
                                Vec3 var2x = param4.apply(param5.get(param6));
                                if (!var1x.closerThan(var2x, (double)param2)) {
                                    return false;
                                } else {
                                    if (var0x.isPresent() && ((WalkTarget)var0x.get()).getSpeedModifier() == param1) {
                                        Vec3 var5x = ((WalkTarget)var0x.get()).getTarget().currentPosition().subtract(var1x);
                                        Vec3 var6x = var2x.subtract(var1x);
                                        if (var5x.dot(var6x) < 0.0) {
                                            return false;
                                        }
                                    }
        
                                    for(int var5 = 0; var5 < 10; ++var5) {
                                        Vec3 var6 = LandRandomPos.getPosAway(param8, 16, 7, var2x);
                                        if (var6 != null) {
                                            param5x.set(new WalkTarget(var6, param1, 0));
                                            break;
                                        }
                                    }
        
                                    return true;
                                }
                            }
                        })
        );
    }
}

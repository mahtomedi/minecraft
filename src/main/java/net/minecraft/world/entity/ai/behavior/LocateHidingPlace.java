package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;

public class LocateHidingPlace {
    public static OneShot<LivingEntity> create(int param0, float param1, int param2) {
        return BehaviorBuilder.create(
            param3 -> param3.<MemoryAccessor, MemoryAccessor, MemoryAccessor, MemoryAccessor, MemoryAccessor, MemoryAccessor, MemoryAccessor>group(
                        param3.absent(MemoryModuleType.WALK_TARGET),
                        param3.registered(MemoryModuleType.HOME),
                        param3.registered(MemoryModuleType.HIDING_PLACE),
                        param3.registered(MemoryModuleType.PATH),
                        param3.registered(MemoryModuleType.LOOK_TARGET),
                        param3.registered(MemoryModuleType.BREED_TARGET),
                        param3.registered(MemoryModuleType.INTERACTION_TARGET)
                    )
                    .apply(
                        param3,
                        (param4, param5, param6, param7, param8, param9, param10) -> (param11, param12, param13) -> {
                                param11.getPoiManager()
                                    .find(param0x -> param0x.is(PoiTypes.HOME), param0x -> true, param12.blockPosition(), param2 + 1, PoiManager.Occupancy.ANY)
                                    .filter(param2x -> param2x.closerToCenterThan(param12.position(), (double)param2))
                                    .or(
                                        () -> param11.getPoiManager()
                                                .getRandom(
                                                    param0x -> param0x.is(PoiTypes.HOME),
                                                    param0x -> true,
                                                    PoiManager.Occupancy.ANY,
                                                    param12.blockPosition(),
                                                    param0,
                                                    param12.getRandom()
                                                )
                                    )
                                    .or(() -> param3.<GlobalPos>tryGet(param5).map(GlobalPos::pos))
                                    .ifPresent(param10x -> {
                                        param7.erase();
                                        param8.erase();
                                        param9.erase();
                                        param10.erase();
                                        param6.set(GlobalPos.of(param11.dimension(), param10x));
                                        if (!param10x.closerToCenterThan(param12.position(), (double)param2)) {
                                            param4.set(new WalkTarget(param10x, param1, param2));
                                        }
                
                                    });
                                return true;
                            }
                    )
        );
    }
}

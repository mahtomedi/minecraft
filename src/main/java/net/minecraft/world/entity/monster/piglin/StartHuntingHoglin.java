package net.minecraft.world.entity.monster.piglin;

import java.util.List;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.hoglin.Hoglin;

public class StartHuntingHoglin {
    public static OneShot<Piglin> create() {
        return BehaviorBuilder.create(
            param0 -> param0.<MemoryAccessor, MemoryAccessor, MemoryAccessor, MemoryAccessor>group(
                        param0.present(MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN),
                        param0.absent(MemoryModuleType.ANGRY_AT),
                        param0.absent(MemoryModuleType.HUNTED_RECENTLY),
                        param0.registered(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS)
                    )
                    .apply(
                        param0,
                        (param1, param2, param3, param4) -> (param3x, param4x, param5) -> {
                                if (!param4x.isBaby()
                                    && !param0.<List>tryGet(param4)
                                        .map(param0x -> param0x.stream().anyMatch(StartHuntingHoglin::hasHuntedRecently))
                                        .isPresent()) {
                                    Hoglin var0x = param0.get(param1);
                                    PiglinAi.setAngerTarget(param4x, var0x);
                                    PiglinAi.dontKillAnyMoreHoglinsForAWhile(param4x);
                                    PiglinAi.broadcastAngerTarget(param4x, var0x);
                                    param0.<List>tryGet(param4).ifPresent(param0x -> param0x.forEach(PiglinAi::dontKillAnyMoreHoglinsForAWhile));
                                    return true;
                                } else {
                                    return false;
                                }
                            }
                    )
        );
    }

    private static boolean hasHuntedRecently(AbstractPiglin param0) {
        return param0.getBrain().hasMemoryValue(MemoryModuleType.HUNTED_RECENTLY);
    }
}

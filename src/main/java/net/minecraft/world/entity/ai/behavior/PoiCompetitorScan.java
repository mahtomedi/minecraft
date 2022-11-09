package net.minecraft.world.entity.ai.behavior;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;

public class PoiCompetitorScan {
    public static BehaviorControl<Villager> create() {
        return BehaviorBuilder.create(
            param0 -> param0.<MemoryAccessor, MemoryAccessor>group(
                        param0.present(MemoryModuleType.JOB_SITE), param0.present(MemoryModuleType.NEAREST_LIVING_ENTITIES)
                    )
                    .apply(
                        param0,
                        (param1, param2) -> (param3, param4, param5) -> {
                                GlobalPos var0x = param0.get(param1);
                                param3.getPoiManager()
                                    .getType(var0x.pos())
                                    .ifPresent(
                                        param4x -> param0.<List<LivingEntity>>get(param2)
                                                .stream()
                                                .filter(param1x -> param1x instanceof Villager && param1x != param4)
                                                .map(param0x -> (Villager)param0x)
                                                .filter(LivingEntity::isAlive)
                                                .filter(param2x -> competesForSameJobsite(var0x, param4x, param2x))
                                                .reduce(param4, PoiCompetitorScan::selectWinner)
                                    );
                                return true;
                            }
                    )
        );
    }

    private static Villager selectWinner(Villager param0, Villager param1) {
        Villager var0;
        Villager var1;
        if (param0.getVillagerXp() > param1.getVillagerXp()) {
            var0 = param0;
            var1 = param1;
        } else {
            var0 = param1;
            var1 = param0;
        }

        var1.getBrain().eraseMemory(MemoryModuleType.JOB_SITE);
        return var0;
    }

    private static boolean competesForSameJobsite(GlobalPos param0, Holder<PoiType> param1, Villager param2) {
        Optional<GlobalPos> var0 = param2.getBrain().getMemory(MemoryModuleType.JOB_SITE);
        return var0.isPresent() && param0.equals(var0.get()) && hasMatchingProfession(param1, param2.getVillagerData().getProfession());
    }

    private static boolean hasMatchingProfession(Holder<PoiType> param0, VillagerProfession param1) {
        return param1.heldJobSite().test(param0);
    }
}

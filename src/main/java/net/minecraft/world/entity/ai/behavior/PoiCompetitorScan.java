package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;

public class PoiCompetitorScan extends Behavior<Villager> {
    final VillagerProfession profession;

    public PoiCompetitorScan(VillagerProfession param0) {
        super(ImmutableMap.of(MemoryModuleType.JOB_SITE, MemoryStatus.VALUE_PRESENT, MemoryModuleType.LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT));
        this.profession = param0;
    }

    protected void start(ServerLevel param0, Villager param1, long param2) {
        GlobalPos var0 = param1.getBrain().getMemory(MemoryModuleType.JOB_SITE).get();
        param0.getPoiManager()
            .getType(var0.pos())
            .ifPresent(
                param2x -> BehaviorUtils.getNearbyVillagersWithCondition(param1, param2xx -> this.competesForSameJobsite(var0, param2x, param2xx))
                        .reduce(param1, PoiCompetitorScan::selectWinner)
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

    private boolean competesForSameJobsite(GlobalPos param0, PoiType param1, Villager param2) {
        return this.hasJobSite(param2)
            && param0.equals(param2.getBrain().getMemory(MemoryModuleType.JOB_SITE).get())
            && this.hasMatchingProfession(param1, param2.getVillagerData().getProfession());
    }

    private boolean hasMatchingProfession(PoiType param0, VillagerProfession param1) {
        return param1.getJobPoiType().getPredicate().test(param0);
    }

    private boolean hasJobSite(Villager param0) {
        return param0.getBrain().getMemory(MemoryModuleType.JOB_SITE).isPresent();
    }
}

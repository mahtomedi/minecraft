package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;

public class ResetProfession extends Behavior<Villager> {
    public ResetProfession() {
        super(ImmutableMap.of(MemoryModuleType.JOB_SITE, MemoryStatus.VALUE_ABSENT));
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, Villager param1) {
        VillagerData var0 = param1.getVillagerData();
        return var0.getProfession() != VillagerProfession.NONE
            && var0.getProfession() != VillagerProfession.NITWIT
            && param1.getVillagerXp() == 0
            && var0.getLevel() <= 1;
    }

    protected void start(ServerLevel param0, Villager param1, long param2) {
        param1.setVillagerData(param1.getVillagerData().setProfession(VillagerProfession.NONE));
        param1.refreshBrain(param0);
    }
}

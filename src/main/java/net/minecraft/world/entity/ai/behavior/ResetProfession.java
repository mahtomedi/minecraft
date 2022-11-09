package net.minecraft.world.entity.ai.behavior;

import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;

public class ResetProfession {
    public static BehaviorControl<Villager> create() {
        return BehaviorBuilder.create(
            param0 -> param0.<MemoryAccessor>group(param0.absent(MemoryModuleType.JOB_SITE))
                    .apply(
                        param0,
                        param0x -> (param0xx, param1, param2) -> {
                                VillagerData var0x = param1.getVillagerData();
                                if (var0x.getProfession() != VillagerProfession.NONE
                                    && var0x.getProfession() != VillagerProfession.NITWIT
                                    && param1.getVillagerXp() == 0
                                    && var0x.getLevel() <= 1) {
                                    param1.setVillagerData(param1.getVillagerData().setProfession(VillagerProfession.NONE));
                                    param1.refreshBrain(param0xx);
                                    return true;
                                } else {
                                    return false;
                                }
                            }
                    )
        );
    }
}

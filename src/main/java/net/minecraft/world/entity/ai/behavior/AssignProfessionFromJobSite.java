package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;

public class AssignProfessionFromJobSite extends Behavior<Villager> {
    public AssignProfessionFromJobSite() {
        super(ImmutableMap.of(MemoryModuleType.POTENTIAL_JOB_SITE, MemoryStatus.VALUE_PRESENT));
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, Villager param1) {
        BlockPos var0 = param1.getBrain().getMemory(MemoryModuleType.POTENTIAL_JOB_SITE).get().pos();
        return var0.closerThan(param1.position(), 2.0) || param1.assignProfessionWhenSpawned();
    }

    protected void start(ServerLevel param0, Villager param1, long param2) {
        GlobalPos var0 = param1.getBrain().getMemory(MemoryModuleType.POTENTIAL_JOB_SITE).get();
        param1.getBrain().eraseMemory(MemoryModuleType.POTENTIAL_JOB_SITE);
        param1.getBrain().setMemory(MemoryModuleType.JOB_SITE, var0);
        param0.broadcastEntityEvent(param1, (byte)14);
        if (param1.getVillagerData().getProfession() == VillagerProfession.NONE) {
            MinecraftServer var1 = param0.getServer();
            Optional.ofNullable(var1.getLevel(var0.dimension()))
                .flatMap(param1x -> param1x.getPoiManager().getType(var0.pos()))
                .flatMap(param0x -> Registry.VILLAGER_PROFESSION.stream().filter(param1x -> param1x.getJobPoiType() == param0x).findFirst())
                .ifPresent(param2x -> {
                    param1.setVillagerData(param1.getVillagerData().setProfession(param2x));
                    param1.refreshBrain(param0);
                });
        }
    }
}

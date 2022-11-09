package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;

public class AssignProfessionFromJobSite {
    public static BehaviorControl<Villager> create() {
        return BehaviorBuilder.create(
            param0 -> param0.<MemoryAccessor, MemoryAccessor>group(
                        param0.present(MemoryModuleType.POTENTIAL_JOB_SITE), param0.registered(MemoryModuleType.JOB_SITE)
                    )
                    .apply(
                        param0,
                        (param1, param2) -> (param3, param4, param5) -> {
                                GlobalPos var0x = param0.get(param1);
                                if (!var0x.pos().closerToCenterThan(param4.position(), 2.0) && !param4.assignProfessionWhenSpawned()) {
                                    return false;
                                } else {
                                    param1.erase();
                                    param2.set(var0x);
                                    param3.broadcastEntityEvent(param4, (byte)14);
                                    if (param4.getVillagerData().getProfession() != VillagerProfession.NONE) {
                                        return true;
                                    } else {
                                        MinecraftServer var1x = param3.getServer();
                                        Optional.ofNullable(var1x.getLevel(var0x.dimension()))
                                            .flatMap(param1x -> param1x.getPoiManager().getType(var0x.pos()))
                                            .flatMap(
                                                param0x -> BuiltInRegistries.VILLAGER_PROFESSION
                                                        .stream()
                                                        .filter(param1x -> param1x.heldJobSite().test(param0x))
                                                        .findFirst()
                                            )
                                            .ifPresent(param2x -> {
                                                param4.setVillagerData(param4.getVillagerData().setProfession(param2x));
                                                param4.refreshBrain(param3);
                                            });
                                        return true;
                                    }
                                }
                            }
                    )
        );
    }
}

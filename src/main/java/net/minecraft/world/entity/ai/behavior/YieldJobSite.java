package net.minecraft.world.entity.ai.behavior;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.pathfinder.Path;

public class YieldJobSite {
    public static BehaviorControl<Villager> create(float param0) {
        return BehaviorBuilder.create(
            param1 -> param1.<MemoryAccessor, MemoryAccessor, MemoryAccessor, MemoryAccessor, MemoryAccessor>group(
                        param1.present(MemoryModuleType.POTENTIAL_JOB_SITE),
                        param1.absent(MemoryModuleType.JOB_SITE),
                        param1.present(MemoryModuleType.NEAREST_LIVING_ENTITIES),
                        param1.registered(MemoryModuleType.WALK_TARGET),
                        param1.registered(MemoryModuleType.LOOK_TARGET)
                    )
                    .apply(
                        param1,
                        (param2, param3, param4, param5, param6) -> (param6x, param7, param8) -> {
                                if (param7.isBaby()) {
                                    return false;
                                } else if (param7.getVillagerData().getProfession() != VillagerProfession.NONE) {
                                    return false;
                                } else {
                                    BlockPos var0x = param1.<GlobalPos>get(param2).pos();
                                    Optional<Holder<PoiType>> var1x = param6x.getPoiManager().getType(var0x);
                                    if (var1x.isEmpty()) {
                                        return true;
                                    } else {
                                        param1.<List<LivingEntity>>get(param4)
                                            .stream()
                                            .filter(param1x -> param1x instanceof Villager && param1x != param7)
                                            .map(param0x -> (Villager)param0x)
                                            .filter(LivingEntity::isAlive)
                                            .filter(param2x -> nearbyWantsJobsite((Holder<PoiType>)var1x.get(), param2x, var0x))
                                            .findFirst()
                                            .ifPresent(param6xx -> {
                                                param5.erase();
                                                param6.erase();
                                                param2.erase();
                                                if (param6xx.getBrain().getMemory(MemoryModuleType.JOB_SITE).isEmpty()) {
                                                    BehaviorUtils.setWalkAndLookTargetMemories(param6xx, var0x, param0, 1);
                                                    param6xx.getBrain()
                                                        .setMemory(MemoryModuleType.POTENTIAL_JOB_SITE, GlobalPos.of(param6x.dimension(), var0x));
                                                    DebugPackets.sendPoiTicketCountPacket(param6x, var0x);
                                                }
                
                                            });
                                        return true;
                                    }
                                }
                            }
                    )
        );
    }

    private static boolean nearbyWantsJobsite(Holder<PoiType> param0, Villager param1, BlockPos param2) {
        boolean var0 = param1.getBrain().getMemory(MemoryModuleType.POTENTIAL_JOB_SITE).isPresent();
        if (var0) {
            return false;
        } else {
            Optional<GlobalPos> var1 = param1.getBrain().getMemory(MemoryModuleType.JOB_SITE);
            VillagerProfession var2 = param1.getVillagerData().getProfession();
            if (var2.heldJobSite().test(param0)) {
                return var1.isEmpty() ? canReachPos(param1, param2, param0.value()) : var1.get().pos().equals(param2);
            } else {
                return false;
            }
        }
    }

    private static boolean canReachPos(PathfinderMob param0, BlockPos param1, PoiType param2) {
        Path var0 = param0.getNavigation().createPath(param1, param2.validRange());
        return var0 != null && var0.canReach();
    }
}

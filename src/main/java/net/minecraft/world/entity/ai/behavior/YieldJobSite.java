package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.pathfinder.Path;

public class YieldJobSite extends Behavior<Villager> {
    private final float speedModifier;

    public YieldJobSite(float param0) {
        super(
            ImmutableMap.of(
                MemoryModuleType.POTENTIAL_JOB_SITE,
                MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.JOB_SITE,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.NEAREST_LIVING_ENTITIES,
                MemoryStatus.VALUE_PRESENT
            )
        );
        this.speedModifier = param0;
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, Villager param1) {
        if (param1.isBaby()) {
            return false;
        } else {
            return param1.getVillagerData().getProfession() == VillagerProfession.NONE;
        }
    }

    protected void start(ServerLevel param0, Villager param1, long param2) {
        BlockPos var0 = param1.getBrain().getMemory(MemoryModuleType.POTENTIAL_JOB_SITE).get().pos();
        Optional<Holder<PoiType>> var1 = param0.getPoiManager().getType(var0);
        if (var1.isPresent()) {
            BehaviorUtils.getNearbyVillagersWithCondition(param1, param2x -> this.nearbyWantsJobsite(var1.get(), param2x, var0))
                .findFirst()
                .ifPresent(param3 -> this.yieldJobSite(param0, param1, param3, var0, param3.getBrain().getMemory(MemoryModuleType.JOB_SITE).isPresent()));
        }
    }

    private boolean nearbyWantsJobsite(Holder<PoiType> param0, Villager param1, BlockPos param2) {
        boolean var0 = param1.getBrain().getMemory(MemoryModuleType.POTENTIAL_JOB_SITE).isPresent();
        if (var0) {
            return false;
        } else {
            Optional<GlobalPos> var1 = param1.getBrain().getMemory(MemoryModuleType.JOB_SITE);
            VillagerProfession var2 = param1.getVillagerData().getProfession();
            if (var2.heldJobSite().test(param0)) {
                return !var1.isPresent() ? this.canReachPos(param1, param2, param0.value()) : var1.get().pos().equals(param2);
            } else {
                return false;
            }
        }
    }

    private void yieldJobSite(ServerLevel param0, Villager param1, Villager param2, BlockPos param3, boolean param4) {
        this.eraseMemories(param1);
        if (!param4) {
            BehaviorUtils.setWalkAndLookTargetMemories(param2, param3, this.speedModifier, 1);
            param2.getBrain().setMemory(MemoryModuleType.POTENTIAL_JOB_SITE, GlobalPos.of(param0.dimension(), param3));
            DebugPackets.sendPoiTicketCountPacket(param0, param3);
        }

    }

    private boolean canReachPos(Villager param0, BlockPos param1, PoiType param2) {
        Path var0 = param0.getNavigation().createPath(param1, param2.validRange());
        return var0 != null && var0.canReach();
    }

    private void eraseMemories(Villager param0) {
        param0.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        param0.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
        param0.getBrain().eraseMemory(MemoryModuleType.POTENTIAL_JOB_SITE);
    }
}

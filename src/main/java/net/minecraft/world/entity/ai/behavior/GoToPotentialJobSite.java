package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Activity;

public class GoToPotentialJobSite extends Behavior<Villager> {
    private static final int TICKS_UNTIL_TIMEOUT = 1200;
    final float speedModifier;

    public GoToPotentialJobSite(float param0) {
        super(ImmutableMap.of(MemoryModuleType.POTENTIAL_JOB_SITE, MemoryStatus.VALUE_PRESENT), 1200);
        this.speedModifier = param0;
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, Villager param1) {
        return param1.getBrain()
            .getActiveNonCoreActivity()
            .map(param0x -> param0x == Activity.IDLE || param0x == Activity.WORK || param0x == Activity.PLAY)
            .orElse(true);
    }

    protected boolean canStillUse(ServerLevel param0, Villager param1, long param2) {
        return param1.getBrain().hasMemoryValue(MemoryModuleType.POTENTIAL_JOB_SITE);
    }

    protected void tick(ServerLevel param0, Villager param1, long param2) {
        BehaviorUtils.setWalkAndLookTargetMemories(param1, param1.getBrain().getMemory(MemoryModuleType.POTENTIAL_JOB_SITE).get().pos(), this.speedModifier, 1);
    }

    protected void stop(ServerLevel param0, Villager param1, long param2) {
        Optional<GlobalPos> var0 = param1.getBrain().getMemory(MemoryModuleType.POTENTIAL_JOB_SITE);
        var0.ifPresent(param1x -> {
            BlockPos var0x = param1x.pos();
            ServerLevel var1x = param0.getServer().getLevel(param1x.dimension());
            if (var1x != null) {
                PoiManager var2x = var1x.getPoiManager();
                if (var2x.exists(var0x, param0x -> true)) {
                    var2x.release(var0x);
                }

                DebugPackets.sendPoiTicketCountPacket(param0, var0x);
            }
        });
        param1.getBrain().eraseMemory(MemoryModuleType.POTENTIAL_JOB_SITE);
    }
}

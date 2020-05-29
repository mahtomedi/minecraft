package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Activity;

public class GoToPotentialJobSite extends Behavior<Villager> {
    final float speedModifier;

    public GoToPotentialJobSite(float param0) {
        super(ImmutableMap.of(MemoryModuleType.POTENTIAL_JOB_SITE, MemoryStatus.VALUE_PRESENT));
        this.speedModifier = param0;
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, Villager param1) {
        return param1.getBrain()
            .getActiveNonCoreActivity()
            .map(param0x -> param0x == Activity.IDLE || param0x == Activity.WORK || param0x == Activity.PLAY)
            .orElse(true);
    }

    protected void start(ServerLevel param0, Villager param1, long param2) {
        BehaviorUtils.setWalkAndLookTargetMemories(param1, param1.getBrain().getMemory(MemoryModuleType.POTENTIAL_JOB_SITE).get().pos(), this.speedModifier, 1);
    }
}

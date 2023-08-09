package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;

public class WorkAtPoi extends Behavior<Villager> {
    private static final int CHECK_COOLDOWN = 300;
    private static final double DISTANCE = 1.73;
    private long lastCheck;

    public WorkAtPoi() {
        super(ImmutableMap.of(MemoryModuleType.JOB_SITE, MemoryStatus.VALUE_PRESENT, MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED));
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, Villager param1) {
        if (param0.getGameTime() - this.lastCheck < 300L) {
            return false;
        } else if (param0.random.nextInt(2) != 0) {
            return false;
        } else {
            this.lastCheck = param0.getGameTime();
            GlobalPos var0 = param1.getBrain().getMemory(MemoryModuleType.JOB_SITE).get();
            return var0.dimension() == param0.dimension() && var0.pos().closerToCenterThan(param1.position(), 1.73);
        }
    }

    protected void start(ServerLevel param0, Villager param1, long param2) {
        Brain<Villager> var0 = param1.getBrain();
        var0.setMemory(MemoryModuleType.LAST_WORKED_AT_POI, param2);
        var0.getMemory(MemoryModuleType.JOB_SITE).ifPresent(param1x -> var0.setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(param1x.pos())));
        param1.playWorkSound();
        this.useWorkstation(param0, param1);
        if (param1.shouldRestock()) {
            param1.restock();
        }

    }

    protected void useWorkstation(ServerLevel param0, Villager param1) {
    }

    protected boolean canStillUse(ServerLevel param0, Villager param1, long param2) {
        Optional<GlobalPos> var0 = param1.getBrain().getMemory(MemoryModuleType.JOB_SITE);
        if (var0.isEmpty()) {
            return false;
        } else {
            GlobalPos var1 = var0.get();
            return var1.dimension() == param0.dimension() && var1.pos().closerToCenterThan(param1.position(), 1.73);
        }
    }
}

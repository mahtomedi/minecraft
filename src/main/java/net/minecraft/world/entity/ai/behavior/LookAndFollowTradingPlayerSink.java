package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;

public class LookAndFollowTradingPlayerSink extends Behavior<Villager> {
    private final float speed;

    public LookAndFollowTradingPlayerSink(float param0) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED), Integer.MAX_VALUE);
        this.speed = param0;
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, Villager param1) {
        Player var0 = param1.getTradingPlayer();
        return param1.isAlive()
            && var0 != null
            && !param1.isInWater()
            && !param1.hurtMarked
            && param1.distanceToSqr(var0) <= 16.0
            && var0.containerMenu != null;
    }

    protected boolean canStillUse(ServerLevel param0, Villager param1, long param2) {
        return this.checkExtraStartConditions(param0, param1);
    }

    protected void start(ServerLevel param0, Villager param1, long param2) {
        this.followPlayer(param1);
    }

    protected void stop(ServerLevel param0, Villager param1, long param2) {
        Brain<?> var0 = param1.getBrain();
        var0.eraseMemory(MemoryModuleType.WALK_TARGET);
        var0.eraseMemory(MemoryModuleType.LOOK_TARGET);
    }

    protected void tick(ServerLevel param0, Villager param1, long param2) {
        this.followPlayer(param1);
    }

    @Override
    protected boolean timedOut(long param0) {
        return false;
    }

    private void followPlayer(Villager param0) {
        EntityPosWrapper var0 = new EntityPosWrapper(param0.getTradingPlayer());
        Brain<?> var1 = param0.getBrain();
        var1.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(var0, this.speed, 2));
        var1.setMemory(MemoryModuleType.LOOK_TARGET, var0);
    }
}

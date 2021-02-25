package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.player.Player;

public class FollowTemptation extends Behavior<PathfinderMob> {
    private final Function<LivingEntity, Float> speedModifier;

    public FollowTemptation(Function<LivingEntity, Float> param0) {
        super(
            ImmutableMap.of(
                MemoryModuleType.LOOK_TARGET,
                MemoryStatus.REGISTERED,
                MemoryModuleType.WALK_TARGET,
                MemoryStatus.REGISTERED,
                MemoryModuleType.TEMPTATION_COOLDOWN_TICKS,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.IS_TEMPTED,
                MemoryStatus.REGISTERED,
                MemoryModuleType.TEMPTING_PLAYER,
                MemoryStatus.VALUE_PRESENT
            )
        );
        this.speedModifier = param0;
    }

    protected float getSpeedModifier(PathfinderMob param0) {
        return this.speedModifier.apply(param0);
    }

    private Optional<Player> getTemptingPlayer(PathfinderMob param0) {
        return param0.getBrain().getMemory(MemoryModuleType.TEMPTING_PLAYER);
    }

    @Override
    protected boolean timedOut(long param0) {
        return false;
    }

    protected boolean canStillUse(ServerLevel param0, PathfinderMob param1, long param2) {
        return this.getTemptingPlayer(param1).isPresent();
    }

    protected void start(ServerLevel param0, PathfinderMob param1, long param2) {
        param1.getBrain().setMemory(MemoryModuleType.IS_TEMPTED, true);
    }

    protected void stop(ServerLevel param0, PathfinderMob param1, long param2) {
        Brain<?> var0 = param1.getBrain();
        var0.setMemory(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, 100);
        var0.setMemory(MemoryModuleType.IS_TEMPTED, false);
        var0.eraseMemory(MemoryModuleType.WALK_TARGET);
        var0.eraseMemory(MemoryModuleType.LOOK_TARGET);
    }

    protected void tick(ServerLevel param0, PathfinderMob param1, long param2) {
        Player var0 = this.getTemptingPlayer(param1).get();
        Brain<?> var1 = param1.getBrain();
        var1.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(var0, true));
        if (param1.distanceToSqr(var0) < 6.25) {
            var1.eraseMemory(MemoryModuleType.WALK_TARGET);
        } else {
            var1.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityTracker(var0, false), this.getSpeedModifier(param1), 2));
        }

    }
}
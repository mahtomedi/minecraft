package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.player.Player;

public class FollowTemptation extends Behavior<PathfinderMob> {
    public static final int TEMPTATION_COOLDOWN = 100;
    public static final double DEFAULT_CLOSE_ENOUGH_DIST = 2.5;
    public static final double BACKED_UP_CLOSE_ENOUGH_DIST = 3.5;
    private final Function<LivingEntity, Float> speedModifier;
    private final Function<LivingEntity, Double> closeEnoughDistance;

    public FollowTemptation(Function<LivingEntity, Float> param0) {
        this(param0, param0x -> 2.5);
    }

    public FollowTemptation(Function<LivingEntity, Float> param0, Function<LivingEntity, Double> param1) {
        super(Util.make(() -> {
            Builder<MemoryModuleType<?>, MemoryStatus> var0 = ImmutableMap.builder();
            var0.put(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED);
            var0.put(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED);
            var0.put(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, MemoryStatus.VALUE_ABSENT);
            var0.put(MemoryModuleType.IS_TEMPTED, MemoryStatus.REGISTERED);
            var0.put(MemoryModuleType.TEMPTING_PLAYER, MemoryStatus.VALUE_PRESENT);
            var0.put(MemoryModuleType.BREED_TARGET, MemoryStatus.VALUE_ABSENT);
            var0.put(MemoryModuleType.IS_PANICKING, MemoryStatus.VALUE_ABSENT);
            return var0.build();
        }));
        this.speedModifier = param0;
        this.closeEnoughDistance = param1;
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
        return this.getTemptingPlayer(param1).isPresent()
            && !param1.getBrain().hasMemoryValue(MemoryModuleType.BREED_TARGET)
            && !param1.getBrain().hasMemoryValue(MemoryModuleType.IS_PANICKING);
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
        double var2 = this.closeEnoughDistance.apply(param1);
        if (param1.distanceToSqr(var0) < Mth.square(var2)) {
            var1.eraseMemory(MemoryModuleType.WALK_TARGET);
        } else {
            var1.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityTracker(var0, false), this.getSpeedModifier(param1), 2));
        }

    }
}

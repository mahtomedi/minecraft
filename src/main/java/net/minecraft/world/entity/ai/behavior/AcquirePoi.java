package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.pathfinder.Path;
import org.apache.commons.lang3.mutable.MutableLong;

public class AcquirePoi {
    public static final int SCAN_RANGE = 48;

    public static BehaviorControl<PathfinderMob> create(
        Predicate<Holder<PoiType>> param0, MemoryModuleType<GlobalPos> param1, boolean param2, Optional<Byte> param3
    ) {
        return create(param0, param1, param1, param2, param3);
    }

    public static BehaviorControl<PathfinderMob> create(
        Predicate<Holder<PoiType>> param0, MemoryModuleType<GlobalPos> param1, MemoryModuleType<GlobalPos> param2, boolean param3, Optional<Byte> param4
    ) {
        int var0 = 5;
        int var1 = 20;
        MutableLong var2 = new MutableLong(0L);
        Long2ObjectMap<AcquirePoi.JitteredLinearRetry> var3 = new Long2ObjectOpenHashMap<>();
        OneShot<PathfinderMob> var4 = BehaviorBuilder.create(
            param6 -> param6.<MemoryAccessor>group(param6.absent(param2))
                    .apply(
                        param6,
                        param5x -> (param6x, param7, param8) -> {
                                if (param3 && param7.isBaby()) {
                                    return false;
                                } else if (var2.getValue() == 0L) {
                                    var2.setValue(param6x.getGameTime() + (long)param6x.random.nextInt(20));
                                    return false;
                                } else if (param6x.getGameTime() < var2.getValue()) {
                                    return false;
                                } else {
                                    var2.setValue(param8 + 20L + (long)param6x.getRandom().nextInt(20));
                                    PoiManager var0x = param6x.getPoiManager();
                                    var3.long2ObjectEntrySet().removeIf(param1x -> !param1x.getValue().isStillValid(param8));
                                    Predicate<BlockPos> var1x = param2x -> {
                                        AcquirePoi.JitteredLinearRetry var0xx = var3.get(param2x.asLong());
                                        if (var0xx == null) {
                                            return true;
                                        } else if (!var0xx.shouldRetry(param8)) {
                                            return false;
                                        } else {
                                            var0xx.markAttempt(param8);
                                            return true;
                                        }
                                    };
                                    Set<Pair<Holder<PoiType>, BlockPos>> var2x = var0x.findAllClosestFirstWithType(
                                            param0, var1x, param7.blockPosition(), 48, PoiManager.Occupancy.HAS_SPACE
                                        )
                                        .limit(5L)
                                        .collect(Collectors.toSet());
                                    Path var3x = findPathToPois(param7, var2x);
                                    if (var3x != null && var3x.canReach()) {
                                        BlockPos var4x = var3x.getTarget();
                                        var0x.getType(var4x).ifPresent(param8x -> {
                                            var0x.take(param0, (param1x, param2x) -> param2x.equals(var4x), var4x, 1);
                                            param5x.set(GlobalPos.of(param6x.dimension(), var4x));
                                            param4.ifPresent(param2x -> param6x.broadcastEntityEvent(param7, param2x));
                                            var3.clear();
                                            DebugPackets.sendPoiTicketCountPacket(param6x, var4x);
                                        });
                                    } else {
                                        for(Pair<Holder<PoiType>, BlockPos> var5x : var2x) {
                                            var3.computeIfAbsent(
                                                ((BlockPos)var5x.getSecond()).asLong(), param2x -> new AcquirePoi.JitteredLinearRetry(param6x.random, param8)
                                            );
                                        }
                                    }
            
                                    return true;
                                }
                            }
                    )
        );
        return param2 == param1
            ? var4
            : BehaviorBuilder.create(param2x -> param2x.<MemoryAccessor>group(param2x.absent(param1)).apply(param2x, param1x -> var4));
    }

    @Nullable
    public static Path findPathToPois(Mob param0, Set<Pair<Holder<PoiType>, BlockPos>> param1) {
        if (param1.isEmpty()) {
            return null;
        } else {
            Set<BlockPos> var0 = new HashSet<>();
            int var1 = 1;

            for(Pair<Holder<PoiType>, BlockPos> var2 : param1) {
                var1 = Math.max(var1, ((PoiType)var2.getFirst().value()).validRange());
                var0.add(var2.getSecond());
            }

            return param0.getNavigation().createPath(var0, var1);
        }
    }

    static class JitteredLinearRetry {
        private static final int MIN_INTERVAL_INCREASE = 40;
        private static final int MAX_INTERVAL_INCREASE = 80;
        private static final int MAX_RETRY_PATHFINDING_INTERVAL = 400;
        private final RandomSource random;
        private long previousAttemptTimestamp;
        private long nextScheduledAttemptTimestamp;
        private int currentDelay;

        JitteredLinearRetry(RandomSource param0, long param1) {
            this.random = param0;
            this.markAttempt(param1);
        }

        public void markAttempt(long param0) {
            this.previousAttemptTimestamp = param0;
            int var0 = this.currentDelay + this.random.nextInt(40) + 40;
            this.currentDelay = Math.min(var0, 400);
            this.nextScheduledAttemptTimestamp = param0 + (long)this.currentDelay;
        }

        public boolean isStillValid(long param0) {
            return param0 - this.previousAttemptTimestamp < 400L;
        }

        public boolean shouldRetry(long param0) {
            return param0 >= this.nextScheduledAttemptTimestamp;
        }

        @Override
        public String toString() {
            return "RetryMarker{, previousAttemptAt="
                + this.previousAttemptTimestamp
                + ", nextScheduledAttemptAt="
                + this.nextScheduledAttemptTimestamp
                + ", currentDelay="
                + this.currentDelay
                + "}";
        }
    }
}

package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.pathfinder.Path;

public class AcquirePoi extends Behavior<PathfinderMob> {
    private final PoiType poiType;
    private final MemoryModuleType<GlobalPos> memoryToAcquire;
    private final boolean onlyIfAdult;
    private final Optional<Byte> onPoiAcquisitionEvent;
    private long nextScheduledStart;
    private final Long2ObjectMap<AcquirePoi.JitteredLinearRetry> batchCache = new Long2ObjectOpenHashMap<>();

    public AcquirePoi(PoiType param0, MemoryModuleType<GlobalPos> param1, MemoryModuleType<GlobalPos> param2, boolean param3, Optional<Byte> param4) {
        super(constructEntryConditionMap(param1, param2));
        this.poiType = param0;
        this.memoryToAcquire = param2;
        this.onlyIfAdult = param3;
        this.onPoiAcquisitionEvent = param4;
    }

    public AcquirePoi(PoiType param0, MemoryModuleType<GlobalPos> param1, boolean param2, Optional<Byte> param3) {
        this(param0, param1, param1, param2, param3);
    }

    private static ImmutableMap<MemoryModuleType<?>, MemoryStatus> constructEntryConditionMap(
        MemoryModuleType<GlobalPos> param0, MemoryModuleType<GlobalPos> param1
    ) {
        Builder<MemoryModuleType<?>, MemoryStatus> var0 = ImmutableMap.builder();
        var0.put(param0, MemoryStatus.VALUE_ABSENT);
        if (param1 != param0) {
            var0.put(param1, MemoryStatus.VALUE_ABSENT);
        }

        return var0.build();
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, PathfinderMob param1) {
        if (this.onlyIfAdult && param1.isBaby()) {
            return false;
        } else if (this.nextScheduledStart == 0L) {
            this.nextScheduledStart = param1.level.getGameTime() + (long)param0.random.nextInt(20);
            return false;
        } else {
            return param0.getGameTime() >= this.nextScheduledStart;
        }
    }

    protected void start(ServerLevel param0, PathfinderMob param1, long param2) {
        this.nextScheduledStart = param2 + 20L + (long)param0.getRandom().nextInt(20);
        PoiManager var0 = param0.getPoiManager();
        this.batchCache.long2ObjectEntrySet().removeIf(param1x -> !param1x.getValue().isStillValid(param2));
        Predicate<BlockPos> var1 = param1x -> {
            AcquirePoi.JitteredLinearRetry var0x = this.batchCache.get(param1x.asLong());
            if (var0x == null) {
                return true;
            } else if (!var0x.shouldRetry(param2)) {
                return false;
            } else {
                var0x.markAttempt(param2);
                return true;
            }
        };
        Set<BlockPos> var2 = var0.findAll(this.poiType.getPredicate(), var1, param1.blockPosition(), 48, PoiManager.Occupancy.HAS_SPACE)
            .limit(5L)
            .collect(Collectors.toSet());
        Path var3 = param1.getNavigation().createPath(var2, this.poiType.getValidRange());
        if (var3 != null && var3.canReach()) {
            BlockPos var4 = var3.getTarget();
            var0.getType(var4).ifPresent(param4 -> {
                var0.take(this.poiType.getPredicate(), param1x -> param1x.equals(var4), var4, 1);
                param1.getBrain().setMemory(this.memoryToAcquire, GlobalPos.of(param0.dimension(), var4));
                this.onPoiAcquisitionEvent.ifPresent(param2x -> param0.broadcastEntityEvent(param1, param2x));
                this.batchCache.clear();
                DebugPackets.sendPoiTicketCountPacket(param0, var4);
            });
        } else {
            for(BlockPos var5 : var2) {
                this.batchCache.computeIfAbsent(var5.asLong(), param2x -> new AcquirePoi.JitteredLinearRetry(param1.level.random, param2));
            }
        }

    }

    static class JitteredLinearRetry {
        private final Random random;
        private long previousAttemptTimestamp;
        private long nextScheduledAttemptTimestamp;
        private int currentDelay;

        JitteredLinearRetry(Random param0, long param1) {
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
                + '}';
        }
    }
}

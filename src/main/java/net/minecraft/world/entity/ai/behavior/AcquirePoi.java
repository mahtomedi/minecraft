package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;
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
    private long lastUpdate;
    private final Long2LongMap batchCache = new Long2LongOpenHashMap();
    private int triedCount;

    public AcquirePoi(PoiType param0, MemoryModuleType<GlobalPos> param1, MemoryModuleType<GlobalPos> param2, boolean param3) {
        super(constructEntryConditionMap(param1, param2));
        this.poiType = param0;
        this.memoryToAcquire = param2;
        this.onlyIfAdult = param3;
    }

    public AcquirePoi(PoiType param0, MemoryModuleType<GlobalPos> param1, boolean param2) {
        this(param0, param1, param1, param2);
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
        } else {
            return param0.getGameTime() - this.lastUpdate >= 20L;
        }
    }

    protected void start(ServerLevel param0, PathfinderMob param1, long param2) {
        this.triedCount = 0;
        this.lastUpdate = param0.getGameTime() + (long)param0.getRandom().nextInt(20);
        PoiManager var0 = param0.getPoiManager();
        Predicate<BlockPos> var1 = param0x -> {
            long var0x = param0x.asLong();
            if (this.batchCache.containsKey(var0x)) {
                return false;
            } else if (++this.triedCount >= 5) {
                return false;
            } else {
                this.batchCache.put(var0x, this.lastUpdate + 40L);
                return true;
            }
        };
        Stream<BlockPos> var2 = var0.findAll(this.poiType.getPredicate(), var1, param1.blockPosition(), 48, PoiManager.Occupancy.HAS_SPACE);
        Path var3 = param1.getNavigation().createPath(var2, this.poiType.getValidRange());
        if (var3 != null && var3.canReach()) {
            BlockPos var4 = var3.getTarget();
            var0.getType(var4).ifPresent(param4 -> {
                var0.take(this.poiType.getPredicate(), param1x -> param1x.equals(var4), var4, 1);
                param1.getBrain().setMemory(this.memoryToAcquire, GlobalPos.of(param0.dimension(), var4));
                DebugPackets.sendPoiTicketCountPacket(param0, var4);
            });
        } else if (this.triedCount < 5) {
            this.batchCache.long2LongEntrySet().removeIf(param0x -> param0x.getLongValue() < this.lastUpdate);
        }

    }
}

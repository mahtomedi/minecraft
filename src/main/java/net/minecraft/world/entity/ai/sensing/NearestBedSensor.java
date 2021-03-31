package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.pathfinder.Path;

public class NearestBedSensor extends Sensor<Mob> {
    private static final int CACHE_TIMEOUT = 40;
    private static final int BATCH_SIZE = 5;
    private static final int RATE = 20;
    private final Long2LongMap batchCache = new Long2LongOpenHashMap();
    private int triedCount;
    private long lastUpdate;

    public NearestBedSensor() {
        super(20);
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.NEAREST_BED);
    }

    protected void doTick(ServerLevel param0, Mob param1) {
        if (param1.isBaby()) {
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
            Stream<BlockPos> var2 = var0.findAll(PoiType.HOME.getPredicate(), var1, param1.blockPosition(), 48, PoiManager.Occupancy.ANY);
            Path var3 = param1.getNavigation().createPath(var2, PoiType.HOME.getValidRange());
            if (var3 != null && var3.canReach()) {
                BlockPos var4 = var3.getTarget();
                Optional<PoiType> var5 = var0.getType(var4);
                if (var5.isPresent()) {
                    param1.getBrain().setMemory(MemoryModuleType.NEAREST_BED, var4);
                }
            } else if (this.triedCount < 5) {
                this.batchCache.long2LongEntrySet().removeIf(param0x -> param0x.getLongValue() < this.lastUpdate);
            }

        }
    }
}

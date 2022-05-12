package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.pathfinder.Path;

public class SetClosestHomeAsWalkTarget extends Behavior<LivingEntity> {
    private static final int CACHE_TIMEOUT = 40;
    private static final int BATCH_SIZE = 5;
    private static final int RATE = 20;
    private static final int OK_DISTANCE_SQR = 4;
    private final float speedModifier;
    private final Long2LongMap batchCache = new Long2LongOpenHashMap();
    private int triedCount;
    private long lastUpdate;

    public SetClosestHomeAsWalkTarget(float param0) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.HOME, MemoryStatus.VALUE_ABSENT));
        this.speedModifier = param0;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel param0, LivingEntity param1) {
        if (param0.getGameTime() - this.lastUpdate < 20L) {
            return false;
        } else {
            PathfinderMob var0 = (PathfinderMob)param1;
            PoiManager var1 = param0.getPoiManager();
            Optional<BlockPos> var2 = var1.findClosest(param0x -> param0x.is(PoiTypes.HOME), param1.blockPosition(), 48, PoiManager.Occupancy.ANY);
            return var2.isPresent() && !(var2.get().distSqr(var0.blockPosition()) <= 4.0);
        }
    }

    @Override
    protected void start(ServerLevel param0, LivingEntity param1, long param2) {
        this.triedCount = 0;
        this.lastUpdate = param0.getGameTime() + (long)param0.getRandom().nextInt(20);
        PathfinderMob var0 = (PathfinderMob)param1;
        PoiManager var1 = param0.getPoiManager();
        Predicate<BlockPos> var2 = param0x -> {
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
        Set<Pair<Holder<PoiType>, BlockPos>> var3 = var1.findAllWithType(
                param0x -> param0x.is(PoiTypes.HOME), var2, param1.blockPosition(), 48, PoiManager.Occupancy.ANY
            )
            .collect(Collectors.toSet());
        Path var4 = AcquirePoi.findPathToPois(var0, var3);
        if (var4 != null && var4.canReach()) {
            BlockPos var5 = var4.getTarget();
            Optional<Holder<PoiType>> var6 = var1.getType(var5);
            if (var6.isPresent()) {
                param1.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(var5, this.speedModifier, 1));
                DebugPackets.sendPoiTicketCountPacket(param0, var5);
            }
        } else if (this.triedCount < 5) {
            this.batchCache.long2LongEntrySet().removeIf(param0x -> param0x.getLongValue() < this.lastUpdate);
        }

    }
}

package net.minecraft.world.entity.ai.behavior;

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
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.pathfinder.Path;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableLong;

public class SetClosestHomeAsWalkTarget {
    private static final int CACHE_TIMEOUT = 40;
    private static final int BATCH_SIZE = 5;
    private static final int RATE = 20;
    private static final int OK_DISTANCE_SQR = 4;

    public static BehaviorControl<PathfinderMob> create(float param0) {
        Long2LongMap var0 = new Long2LongOpenHashMap();
        MutableLong var1 = new MutableLong(0L);
        return BehaviorBuilder.create(
            param3 -> param3.<MemoryAccessor, MemoryAccessor>group(param3.absent(MemoryModuleType.WALK_TARGET), param3.absent(MemoryModuleType.HOME))
                    .apply(
                        param3,
                        (param3x, param4) -> (param4x, param5, param6) -> {
                                if (param4x.getGameTime() - var1.getValue() < 20L) {
                                    return false;
                                } else {
                                    PoiManager var0x = param4x.getPoiManager();
                                    Optional<BlockPos> var1x = var0x.findClosest(
                                        param0x -> param0x.is(PoiTypes.HOME), param5.blockPosition(), 48, PoiManager.Occupancy.ANY
                                    );
                                    if (!var1x.isEmpty() && !(var1x.get().distSqr(param5.blockPosition()) <= 4.0)) {
                                        MutableInt var2x = new MutableInt(0);
                                        var1.setValue(param4x.getGameTime() + (long)param4x.getRandom().nextInt(20));
                                        Predicate<BlockPos> var3x = param3xxx -> {
                                            long var0xx = param3xxx.asLong();
                                            if (var0.containsKey(var0xx)) {
                                                return false;
                                            } else if (var2x.incrementAndGet() >= 5) {
                                                return false;
                                            } else {
                                                var0.put(var0xx, var1.getValue() + 40L);
                                                return true;
                                            }
                                        };
                                        Set<Pair<Holder<PoiType>, BlockPos>> var4x = var0x.findAllWithType(
                                                param0x -> param0x.is(PoiTypes.HOME), var3x, param5.blockPosition(), 48, PoiManager.Occupancy.ANY
                                            )
                                            .collect(Collectors.toSet());
                                        Path var5 = AcquirePoi.findPathToPois(param5, var4x);
                                        if (var5 != null && var5.canReach()) {
                                            BlockPos var6 = var5.getTarget();
                                            Optional<Holder<PoiType>> var7 = var0x.getType(var6);
                                            if (var7.isPresent()) {
                                                param3x.set(new WalkTarget(var6, param0, 1));
                                                DebugPackets.sendPoiTicketCountPacket(param4x, var6);
                                            }
                                        } else if (var2x.getValue() < 5) {
                                            var0.long2LongEntrySet().removeIf(param1x -> param1x.getLongValue() < var1.getValue());
                                        }
            
                                        return true;
                                    } else {
                                        return false;
                                    }
                                }
                            }
                    )
        );
    }
}

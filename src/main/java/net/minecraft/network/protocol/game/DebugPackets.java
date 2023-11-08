package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.GameTestAddMarkerDebugPayload;
import net.minecraft.network.protocol.common.custom.GameTestClearMarkersDebugPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Nameable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class DebugPackets {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void sendGameTestAddMarker(ServerLevel param0, BlockPos param1, String param2, int param3, int param4) {
        sendPacketToAllPlayers(param0, new GameTestAddMarkerDebugPayload(param1, param3, param2, param4));
    }

    public static void sendGameTestClearPacket(ServerLevel param0) {
        sendPacketToAllPlayers(param0, new GameTestClearMarkersDebugPayload());
    }

    public static void sendPoiPacketsForChunk(ServerLevel param0, ChunkPos param1) {
    }

    public static void sendPoiAddedPacket(ServerLevel param0, BlockPos param1) {
        sendVillageSectionsPacket(param0, param1);
    }

    public static void sendPoiRemovedPacket(ServerLevel param0, BlockPos param1) {
        sendVillageSectionsPacket(param0, param1);
    }

    public static void sendPoiTicketCountPacket(ServerLevel param0, BlockPos param1) {
        sendVillageSectionsPacket(param0, param1);
    }

    private static void sendVillageSectionsPacket(ServerLevel param0, BlockPos param1) {
    }

    public static void sendPathFindingPacket(Level param0, Mob param1, @Nullable Path param2, float param3) {
    }

    public static void sendNeighborsUpdatePacket(Level param0, BlockPos param1) {
    }

    public static void sendStructurePacket(WorldGenLevel param0, StructureStart param1) {
    }

    public static void sendGoalSelector(Level param0, Mob param1, GoalSelector param2) {
    }

    public static void sendRaids(ServerLevel param0, Collection<Raid> param1) {
    }

    public static void sendEntityBrain(LivingEntity param0) {
    }

    public static void sendBeeInfo(Bee param0) {
    }

    public static void sendBreezeInfo(Breeze param0) {
    }

    public static void sendGameEventInfo(Level param0, GameEvent param1, Vec3 param2) {
    }

    public static void sendGameEventListenerInfo(Level param0, GameEventListener param1) {
    }

    public static void sendHiveInfo(Level param0, BlockPos param1, BlockState param2, BeehiveBlockEntity param3) {
    }

    private static List<String> getMemoryDescriptions(LivingEntity param0, long param1) {
        Map<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> var0 = param0.getBrain().getMemories();
        List<String> var1 = Lists.newArrayList();

        for(Entry<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> var2 : var0.entrySet()) {
            MemoryModuleType<?> var3 = var2.getKey();
            Optional<? extends ExpirableValue<?>> var4 = var2.getValue();
            String var8;
            if (var4.isPresent()) {
                ExpirableValue<?> var5 = var4.get();
                Object var6 = var5.getValue();
                if (var3 == MemoryModuleType.HEARD_BELL_TIME) {
                    long var7 = param1 - (Long)var6;
                    var8 = var7 + " ticks ago";
                } else if (var5.canExpire()) {
                    var8 = getShortDescription((ServerLevel)param0.level(), var6) + " (ttl: " + var5.getTimeToLive() + ")";
                } else {
                    var8 = getShortDescription((ServerLevel)param0.level(), var6);
                }
            } else {
                var8 = "-";
            }

            var1.add(BuiltInRegistries.MEMORY_MODULE_TYPE.getKey(var3).getPath() + ": " + var8);
        }

        var1.sort(String::compareTo);
        return var1;
    }

    private static String getShortDescription(ServerLevel param0, @Nullable Object param1) {
        if (param1 == null) {
            return "-";
        } else if (param1 instanceof UUID) {
            return getShortDescription(param0, param0.getEntity((UUID)param1));
        } else if (param1 instanceof LivingEntity) {
            Entity var0 = (Entity)param1;
            return DebugEntityNameGenerator.getEntityName(var0);
        } else if (param1 instanceof Nameable) {
            return ((Nameable)param1).getName().getString();
        } else if (param1 instanceof WalkTarget) {
            return getShortDescription(param0, ((WalkTarget)param1).getTarget());
        } else if (param1 instanceof EntityTracker) {
            return getShortDescription(param0, ((EntityTracker)param1).getEntity());
        } else if (param1 instanceof GlobalPos) {
            return getShortDescription(param0, ((GlobalPos)param1).pos());
        } else if (param1 instanceof BlockPosTracker) {
            return getShortDescription(param0, ((BlockPosTracker)param1).currentBlockPosition());
        } else if (param1 instanceof DamageSource) {
            Entity var1 = ((DamageSource)param1).getEntity();
            return var1 == null ? param1.toString() : getShortDescription(param0, var1);
        } else if (!(param1 instanceof Collection)) {
            return param1.toString();
        } else {
            List<String> var2 = Lists.newArrayList();

            for(Object var3 : (Iterable)param1) {
                var2.add(getShortDescription(param0, var3));
            }

            return var2.toString();
        }
    }

    private static void sendPacketToAllPlayers(ServerLevel param0, CustomPacketPayload param1) {
        Packet<?> var0 = new ClientboundCustomPayloadPacket(param1);

        for(ServerPlayer var1 : param0.players()) {
            var1.connection.send(var0);
        }

    }
}

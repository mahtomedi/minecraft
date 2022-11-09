package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringUtil;
import net.minecraft.world.Container;
import net.minecraft.world.Nameable;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
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
        FriendlyByteBuf var0 = new FriendlyByteBuf(Unpooled.buffer());
        var0.writeBlockPos(param1);
        var0.writeInt(param3);
        var0.writeUtf(param2);
        var0.writeInt(param4);
        sendPacketToAllPlayers(param0, var0, ClientboundCustomPayloadPacket.DEBUG_GAME_TEST_ADD_MARKER);
    }

    public static void sendGameTestClearPacket(ServerLevel param0) {
        FriendlyByteBuf var0 = new FriendlyByteBuf(Unpooled.buffer());
        sendPacketToAllPlayers(param0, var0, ClientboundCustomPayloadPacket.DEBUG_GAME_TEST_CLEAR);
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
        if (param0 instanceof ServerLevel) {
            ;
        }
    }

    public static void sendRaids(ServerLevel param0, Collection<Raid> param1) {
    }

    public static void sendEntityBrain(LivingEntity param0) {
    }

    public static void sendBeeInfo(Bee param0) {
    }

    public static void sendGameEventInfo(Level param0, GameEvent param1, Vec3 param2) {
    }

    public static void sendGameEventListenerInfo(Level param0, GameEventListener param1) {
    }

    public static void sendHiveInfo(Level param0, BlockPos param1, BlockState param2, BeehiveBlockEntity param3) {
    }

    private static void writeBrain(LivingEntity param0, FriendlyByteBuf param1) {
        Brain<?> var0 = param0.getBrain();
        long var1 = param0.level.getGameTime();
        if (param0 instanceof InventoryCarrier) {
            Container var2 = ((InventoryCarrier)param0).getInventory();
            param1.writeUtf(var2.isEmpty() ? "" : var2.toString());
        } else {
            param1.writeUtf("");
        }

        param1.writeOptional(
            var0.hasMemoryValue(MemoryModuleType.PATH) ? var0.getMemory(MemoryModuleType.PATH) : Optional.empty(),
            (param0x, param1x) -> param1x.writeToStream(param0x)
        );
        if (param0 instanceof Villager var3) {
            boolean var4 = var3.wantsToSpawnGolem(var1);
            param1.writeBoolean(var4);
        } else {
            param1.writeBoolean(false);
        }

        if (param0.getType() == EntityType.WARDEN) {
            Warden var5 = (Warden)param0;
            param1.writeInt(var5.getClientAngerLevel());
        } else {
            param1.writeInt(-1);
        }

        param1.writeCollection(var0.getActiveActivities(), (param0x, param1x) -> param0x.writeUtf(param1x.getName()));
        Set<String> var6 = var0.getRunningBehaviors().stream().map(BehaviorControl::debugString).collect(Collectors.toSet());
        param1.writeCollection(var6, FriendlyByteBuf::writeUtf);
        param1.writeCollection(getMemoryDescriptions(param0, var1), (param0x, param1x) -> {
            String var0x = StringUtil.truncateStringIfNecessary(param1x, 255, true);
            param0x.writeUtf(var0x);
        });
        if (param0 instanceof Villager) {
            Set<BlockPos> var7 = Stream.of(MemoryModuleType.JOB_SITE, MemoryModuleType.HOME, MemoryModuleType.MEETING_POINT)
                .map(var0::getMemory)
                .flatMap(Optional::stream)
                .map(GlobalPos::pos)
                .collect(Collectors.toSet());
            param1.writeCollection(var7, FriendlyByteBuf::writeBlockPos);
        } else {
            param1.writeVarInt(0);
        }

        if (param0 instanceof Villager) {
            Set<BlockPos> var8 = Stream.of(MemoryModuleType.POTENTIAL_JOB_SITE)
                .map(var0::getMemory)
                .flatMap(Optional::stream)
                .map(GlobalPos::pos)
                .collect(Collectors.toSet());
            param1.writeCollection(var8, FriendlyByteBuf::writeBlockPos);
        } else {
            param1.writeVarInt(0);
        }

        if (param0 instanceof Villager) {
            Map<UUID, Object2IntMap<GossipType>> var9 = ((Villager)param0).getGossips().getGossipEntries();
            List<String> var10 = Lists.newArrayList();
            var9.forEach((param1x, param2) -> {
                String var0x = DebugEntityNameGenerator.getEntityName(param1x);
                param2.forEach((param2x, param3) -> var10.add(var0x + ": " + param2x + ": " + param3));
            });
            param1.writeCollection(var10, FriendlyByteBuf::writeUtf);
        } else {
            param1.writeVarInt(0);
        }

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
                    var8 = getShortDescription((ServerLevel)param0.level, var6) + " (ttl: " + var5.getTimeToLive() + ")";
                } else {
                    var8 = getShortDescription((ServerLevel)param0.level, var6);
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
        } else if (param1 instanceof EntityDamageSource) {
            Entity var1 = ((EntityDamageSource)param1).getEntity();
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

    private static void sendPacketToAllPlayers(ServerLevel param0, FriendlyByteBuf param1, ResourceLocation param2) {
        Packet<?> var0 = new ClientboundCustomPayloadPacket(param2, param1);

        for(Player var1 : param0.players()) {
            ((ServerPlayer)var1).connection.send(var0);
        }

    }
}

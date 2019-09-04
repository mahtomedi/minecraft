package net.minecraft.network.protocol.game;

import io.netty.buffer.Unpooled;
import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.pathfinder.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DebugPackets {
    private static final Logger LOGGER = LogManager.getLogger();

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
    }

    public static void sendPoiRemovedPacket(ServerLevel param0, BlockPos param1) {
    }

    public static void sendPoiTicketCountPacket(ServerLevel param0, BlockPos param1) {
    }

    public static void sendPathFindingPacket(Level param0, Mob param1, @Nullable Path param2, float param3) {
    }

    public static void sendNeighborsUpdatePacket(Level param0, BlockPos param1) {
    }

    public static void sendStructurePacket(LevelAccessor param0, StructureStart param1) {
    }

    public static void sendGoalSelector(Level param0, Mob param1, GoalSelector param2) {
    }

    public static void sendRaids(ServerLevel param0, Collection<Raid> param1) {
    }

    public static void sendEntityBrain(LivingEntity param0) {
    }

    private static void sendPacketToAllPlayers(ServerLevel param0, FriendlyByteBuf param1, ResourceLocation param2) {
        Packet<?> var0 = new ClientboundCustomPayloadPacket(param2, param1);

        for(Player var1 : param0.getLevel().players()) {
            ((ServerPlayer)var1).connection.send(var0);
        }

    }
}

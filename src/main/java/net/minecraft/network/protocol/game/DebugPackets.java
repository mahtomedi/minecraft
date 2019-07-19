package net.minecraft.network.protocol.game;

import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
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
}

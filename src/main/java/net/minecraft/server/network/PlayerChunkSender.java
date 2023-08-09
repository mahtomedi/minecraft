package net.minecraft.server.network;

import com.google.common.collect.Comparators;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.minecraft.network.protocol.game.ClientboundChunkBatchFinishedPacket;
import net.minecraft.network.protocol.game.ClientboundChunkBatchStartPacket;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.slf4j.Logger;

public class PlayerChunkSender {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final float MIN_CHUNKS_PER_TICK = 0.01F;
    public static final float MAX_CHUNKS_PER_TICK = 64.0F;
    private static final float START_CHUNKS_PER_TICK = 9.0F;
    private static final int MAX_UNACKNOWLEDGED_BATCHES = 10;
    private final LongSet pendingChunks = new LongOpenHashSet();
    private final boolean memoryConnection;
    private float desiredChunksPerTick = 9.0F;
    private float batchQuota;
    private int unacknowledgedBatches;
    private int maxUnacknowledgedBatches = 1;

    public PlayerChunkSender(boolean param0) {
        this.memoryConnection = param0;
    }

    public void markChunkPendingToSend(LevelChunk param0) {
        this.pendingChunks.add(param0.getPos().toLong());
    }

    public void dropChunk(ServerPlayer param0, ChunkPos param1) {
        if (!this.pendingChunks.remove(param1.toLong()) && param0.isAlive()) {
            param0.connection.send(new ClientboundForgetLevelChunkPacket(param1));
        }

    }

    public void sendNextChunks(ServerPlayer param0) {
        if (this.unacknowledgedBatches < this.maxUnacknowledgedBatches) {
            float var0 = Math.max(1.0F, this.desiredChunksPerTick);
            this.batchQuota = Math.min(this.batchQuota + this.desiredChunksPerTick, var0);
            if (!(this.batchQuota < 1.0F)) {
                if (!this.pendingChunks.isEmpty()) {
                    ServerLevel var1 = param0.serverLevel();
                    ChunkMap var2 = var1.getChunkSource().chunkMap;
                    List<LevelChunk> var3 = this.collectChunksToSend(var2, param0.chunkPosition());
                    if (!var3.isEmpty()) {
                        ServerGamePacketListenerImpl var4 = param0.connection;
                        ++this.unacknowledgedBatches;
                        var4.send(new ClientboundChunkBatchStartPacket());

                        for(LevelChunk var5 : var3) {
                            sendChunk(var4, var1, var5);
                        }

                        var4.send(new ClientboundChunkBatchFinishedPacket(var3.size()));
                        this.batchQuota -= (float)var3.size();
                    }
                }
            }
        }
    }

    private static void sendChunk(ServerGamePacketListenerImpl param0, ServerLevel param1, LevelChunk param2) {
        param0.send(new ClientboundLevelChunkWithLightPacket(param2, param1.getLightEngine(), null, null));
        ChunkPos var0 = param2.getPos();
        DebugPackets.sendPoiPacketsForChunk(param1, var0);
    }

    private List<LevelChunk> collectChunksToSend(ChunkMap param0, ChunkPos param1) {
        int var0 = Mth.floor(this.batchQuota);
        List<LevelChunk> var2;
        if (!this.memoryConnection && this.pendingChunks.size() > var0) {
            var2 = this.pendingChunks
                .stream()
                .collect(Comparators.least(var0, Comparator.comparingInt(param1::distanceSquared)))
                .stream()
                .mapToLong(Long::longValue)
                .peek(this.pendingChunks::remove)
                .mapToObj(param0::getTickingChunkIfPresent)
                .filter(Objects::nonNull)
                .toList();
        } else {
            var2 = this.pendingChunks
                .longStream()
                .mapToObj(param0::getTickingChunkIfPresent)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(param1x -> param1.distanceSquared(param1x.getPos())))
                .toList();
            this.pendingChunks.clear();
        }

        return var2;
    }

    public void onChunkBatchReceivedByClient(float param0) {
        --this.unacknowledgedBatches;
        this.desiredChunksPerTick = Double.isNaN((double)param0) ? 0.01F : Mth.clamp(param0, 0.01F, 64.0F);
        if (this.unacknowledgedBatches == 0) {
            this.batchQuota = 1.0F;
        }

        this.maxUnacknowledgedBatches = 10;
    }

    public boolean isPending(long param0) {
        return this.pendingChunks.contains(param0);
    }
}

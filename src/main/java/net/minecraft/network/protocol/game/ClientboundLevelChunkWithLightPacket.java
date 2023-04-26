package net.minecraft.network.protocol.game;

import java.util.BitSet;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;

public class ClientboundLevelChunkWithLightPacket implements Packet<ClientGamePacketListener> {
    private final int x;
    private final int z;
    private final ClientboundLevelChunkPacketData chunkData;
    private final ClientboundLightUpdatePacketData lightData;

    public ClientboundLevelChunkWithLightPacket(LevelChunk param0, LevelLightEngine param1, @Nullable BitSet param2, @Nullable BitSet param3) {
        ChunkPos var0 = param0.getPos();
        this.x = var0.x;
        this.z = var0.z;
        this.chunkData = new ClientboundLevelChunkPacketData(param0);
        this.lightData = new ClientboundLightUpdatePacketData(var0, param1, param2, param3);
    }

    public ClientboundLevelChunkWithLightPacket(FriendlyByteBuf param0) {
        this.x = param0.readInt();
        this.z = param0.readInt();
        this.chunkData = new ClientboundLevelChunkPacketData(param0, this.x, this.z);
        this.lightData = new ClientboundLightUpdatePacketData(param0, this.x, this.z);
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeInt(this.x);
        param0.writeInt(this.z);
        this.chunkData.write(param0);
        this.lightData.write(param0);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleLevelChunkWithLight(this);
    }

    public int getX() {
        return this.x;
    }

    public int getZ() {
        return this.z;
    }

    public ClientboundLevelChunkPacketData getChunkData() {
        return this.chunkData;
    }

    public ClientboundLightUpdatePacketData getLightData() {
        return this.lightData;
    }
}

package net.minecraft.network.protocol.game;

import java.util.BitSet;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.lighting.LevelLightEngine;

public class ClientboundLightUpdatePacket implements Packet<ClientGamePacketListener> {
    private final int x;
    private final int z;
    private final ClientboundLightUpdatePacketData lightData;

    public ClientboundLightUpdatePacket(ChunkPos param0, LevelLightEngine param1, @Nullable BitSet param2, @Nullable BitSet param3) {
        this.x = param0.x;
        this.z = param0.z;
        this.lightData = new ClientboundLightUpdatePacketData(param0, param1, param2, param3);
    }

    public ClientboundLightUpdatePacket(FriendlyByteBuf param0) {
        this.x = param0.readVarInt();
        this.z = param0.readVarInt();
        this.lightData = new ClientboundLightUpdatePacketData(param0, this.x, this.z);
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.x);
        param0.writeVarInt(this.z);
        this.lightData.write(param0);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleLightUpdatePacket(this);
    }

    public int getX() {
        return this.x;
    }

    public int getZ() {
        return this.z;
    }

    public ClientboundLightUpdatePacketData getLightData() {
        return this.lightData;
    }
}

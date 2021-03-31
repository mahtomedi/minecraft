package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundForgetLevelChunkPacket implements Packet<ClientGamePacketListener> {
    private final int x;
    private final int z;

    public ClientboundForgetLevelChunkPacket(int param0, int param1) {
        this.x = param0;
        this.z = param1;
    }

    public ClientboundForgetLevelChunkPacket(FriendlyByteBuf param0) {
        this.x = param0.readInt();
        this.z = param0.readInt();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeInt(this.x);
        param0.writeInt(this.z);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleForgetLevelChunk(this);
    }

    public int getX() {
        return this.x;
    }

    public int getZ() {
        return this.z;
    }
}

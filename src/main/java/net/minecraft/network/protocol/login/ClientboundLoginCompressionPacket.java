package net.minecraft.network.protocol.login;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundLoginCompressionPacket implements Packet<ClientLoginPacketListener> {
    private final int compressionThreshold;

    public ClientboundLoginCompressionPacket(int param0) {
        this.compressionThreshold = param0;
    }

    public ClientboundLoginCompressionPacket(FriendlyByteBuf param0) {
        this.compressionThreshold = param0.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.compressionThreshold);
    }

    public void handle(ClientLoginPacketListener param0) {
        param0.handleCompression(this);
    }

    public int getCompressionThreshold() {
        return this.compressionThreshold;
    }
}

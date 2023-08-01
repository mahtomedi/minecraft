package net.minecraft.network.protocol.handshake;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ClientIntentionPacket(int protocolVersion, String hostName, int port, ClientIntent intention) implements Packet<ServerHandshakePacketListener> {
    private static final int MAX_HOST_LENGTH = 255;

    @Deprecated
    public ClientIntentionPacket(int param0, String param1, int param2, ClientIntent param3) {
        this.protocolVersion = param0;
        this.hostName = param1;
        this.port = param2;
        this.intention = param3;
    }

    public ClientIntentionPacket(FriendlyByteBuf param0) {
        this(param0.readVarInt(), param0.readUtf(255), param0.readUnsignedShort(), ClientIntent.byId(param0.readVarInt()));
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.protocolVersion);
        param0.writeUtf(this.hostName);
        param0.writeShort(this.port);
        param0.writeVarInt(this.intention.id());
    }

    public void handle(ServerHandshakePacketListener param0) {
        param0.handleIntention(this);
    }

    @Override
    public ConnectionProtocol nextProtocol() {
        return this.intention.protocol();
    }
}

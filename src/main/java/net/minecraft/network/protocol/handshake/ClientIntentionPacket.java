package net.minecraft.network.protocol.handshake;

import net.minecraft.SharedConstants;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientIntentionPacket implements Packet<ServerHandshakePacketListener> {
    private final int protocolVersion;
    private final String hostName;
    private final int port;
    private final ConnectionProtocol intention;

    @OnlyIn(Dist.CLIENT)
    public ClientIntentionPacket(String param0, int param1, ConnectionProtocol param2) {
        this.protocolVersion = SharedConstants.getCurrentVersion().getProtocolVersion();
        this.hostName = param0;
        this.port = param1;
        this.intention = param2;
    }

    public ClientIntentionPacket(FriendlyByteBuf param0) {
        this.protocolVersion = param0.readVarInt();
        this.hostName = param0.readUtf(255);
        this.port = param0.readUnsignedShort();
        this.intention = ConnectionProtocol.getById(param0.readVarInt());
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.protocolVersion);
        param0.writeUtf(this.hostName);
        param0.writeShort(this.port);
        param0.writeVarInt(this.intention.getId());
    }

    public void handle(ServerHandshakePacketListener param0) {
        param0.handleIntention(this);
    }

    public ConnectionProtocol getIntention() {
        return this.intention;
    }

    public int getProtocolVersion() {
        return this.protocolVersion;
    }
}

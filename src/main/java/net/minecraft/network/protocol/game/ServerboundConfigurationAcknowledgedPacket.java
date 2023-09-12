package net.minecraft.network.protocol.game;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ServerboundConfigurationAcknowledgedPacket() implements Packet<ServerGamePacketListener> {
    public ServerboundConfigurationAcknowledgedPacket(FriendlyByteBuf param0) {
        this();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleConfigurationAcknowledged(this);
    }

    @Override
    public ConnectionProtocol nextProtocol() {
        return ConnectionProtocol.CONFIGURATION;
    }
}

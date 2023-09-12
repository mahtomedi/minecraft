package net.minecraft.network.protocol.game;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ClientboundStartConfigurationPacket() implements Packet<ClientGamePacketListener> {
    public ClientboundStartConfigurationPacket(FriendlyByteBuf param0) {
        this();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleConfigurationStart(this);
    }

    @Override
    public ConnectionProtocol nextProtocol() {
        return ConnectionProtocol.CONFIGURATION;
    }
}

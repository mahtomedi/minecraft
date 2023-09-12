package net.minecraft.network.protocol.configuration;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ServerboundFinishConfigurationPacket() implements Packet<ServerConfigurationPacketListener> {
    public ServerboundFinishConfigurationPacket(FriendlyByteBuf param0) {
        this();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
    }

    public void handle(ServerConfigurationPacketListener param0) {
        param0.handleConfigurationFinished(this);
    }

    @Override
    public ConnectionProtocol nextProtocol() {
        return ConnectionProtocol.PLAY;
    }
}

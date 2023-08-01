package net.minecraft.network.protocol.configuration;

import java.lang.runtime.ObjectMethods;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public final class ClientboundFinishConfigurationPacket extends Record implements Packet<ClientConfigurationPacketListener> {
    public ClientboundFinishConfigurationPacket(FriendlyByteBuf param0) {
        this();
    }

    public ClientboundFinishConfigurationPacket() {
    }

    @Override
    public void write(FriendlyByteBuf param0) {
    }

    public void handle(ClientConfigurationPacketListener param0) {
        param0.handleConfigurationFinished(this);
    }

    @Override
    public ConnectionProtocol nextProtocol() {
        return ConnectionProtocol.PLAY;
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap<"toString",ClientboundFinishConfigurationPacket,"">(this);
    }

    @Override
    public final int hashCode() {
        return ObjectMethods.bootstrap<"hashCode",ClientboundFinishConfigurationPacket,"">(this);
    }

    @Override
    public final boolean equals(Object param0) {
        return ObjectMethods.bootstrap<"equals",ClientboundFinishConfigurationPacket,"">(this, param0);
    }
}

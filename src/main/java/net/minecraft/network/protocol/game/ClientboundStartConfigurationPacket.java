package net.minecraft.network.protocol.game;

import java.lang.runtime.ObjectMethods;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public final class ClientboundStartConfigurationPacket extends Record implements Packet<ClientGamePacketListener> {
    public ClientboundStartConfigurationPacket(FriendlyByteBuf param0) {
        this();
    }

    public ClientboundStartConfigurationPacket() {
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

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap<"toString",ClientboundStartConfigurationPacket,"">(this);
    }

    @Override
    public final int hashCode() {
        return ObjectMethods.bootstrap<"hashCode",ClientboundStartConfigurationPacket,"">(this);
    }

    @Override
    public final boolean equals(Object param0) {
        return ObjectMethods.bootstrap<"equals",ClientboundStartConfigurationPacket,"">(this, param0);
    }
}

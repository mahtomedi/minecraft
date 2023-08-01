package net.minecraft.network.protocol.game;

import java.lang.runtime.ObjectMethods;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public final class ServerboundConfigurationAcknowledgedPacket extends Record implements Packet<ServerGamePacketListener> {
    public ServerboundConfigurationAcknowledgedPacket(FriendlyByteBuf param0) {
        this();
    }

    public ServerboundConfigurationAcknowledgedPacket() {
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

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap<"toString",ServerboundConfigurationAcknowledgedPacket,"">(this);
    }

    @Override
    public final int hashCode() {
        return ObjectMethods.bootstrap<"hashCode",ServerboundConfigurationAcknowledgedPacket,"">(this);
    }

    @Override
    public final boolean equals(Object param0) {
        return ObjectMethods.bootstrap<"equals",ServerboundConfigurationAcknowledgedPacket,"">(this, param0);
    }
}

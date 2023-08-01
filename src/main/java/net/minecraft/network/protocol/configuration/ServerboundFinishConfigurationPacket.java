package net.minecraft.network.protocol.configuration;

import java.lang.runtime.ObjectMethods;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public final class ServerboundFinishConfigurationPacket extends Record implements Packet<ServerConfigurationPacketListener> {
    public ServerboundFinishConfigurationPacket(FriendlyByteBuf param0) {
        this();
    }

    public ServerboundFinishConfigurationPacket() {
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

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap<"toString",ServerboundFinishConfigurationPacket,"">(this);
    }

    @Override
    public final int hashCode() {
        return ObjectMethods.bootstrap<"hashCode",ServerboundFinishConfigurationPacket,"">(this);
    }

    @Override
    public final boolean equals(Object param0) {
        return ObjectMethods.bootstrap<"equals",ServerboundFinishConfigurationPacket,"">(this, param0);
    }
}

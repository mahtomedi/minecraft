package net.minecraft.network.protocol.login;

import java.lang.runtime.ObjectMethods;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public final class ServerboundLoginAcknowledgedPacket extends Record implements Packet<ServerLoginPacketListener> {
    public ServerboundLoginAcknowledgedPacket(FriendlyByteBuf param0) {
        this();
    }

    public ServerboundLoginAcknowledgedPacket() {
    }

    @Override
    public void write(FriendlyByteBuf param0) {
    }

    public void handle(ServerLoginPacketListener param0) {
        param0.handleLoginAcknowledgement(this);
    }

    @Override
    public ConnectionProtocol nextProtocol() {
        return ConnectionProtocol.CONFIGURATION;
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap<"toString",ServerboundLoginAcknowledgedPacket,"">(this);
    }

    @Override
    public final int hashCode() {
        return ObjectMethods.bootstrap<"hashCode",ServerboundLoginAcknowledgedPacket,"">(this);
    }

    @Override
    public final boolean equals(Object param0) {
        return ObjectMethods.bootstrap<"equals",ServerboundLoginAcknowledgedPacket,"">(this, param0);
    }
}

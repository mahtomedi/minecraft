package net.minecraft.network.protocol.game;

import java.lang.runtime.ObjectMethods;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public final class ClientboundChunkBatchStartPacket extends Record implements Packet<ClientGamePacketListener> {
    public ClientboundChunkBatchStartPacket(FriendlyByteBuf param0) {
        this();
    }

    public ClientboundChunkBatchStartPacket() {
    }

    @Override
    public void write(FriendlyByteBuf param0) {
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleChunkBatchStart(this);
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap<"toString",ClientboundChunkBatchStartPacket,"">(this);
    }

    @Override
    public final int hashCode() {
        return ObjectMethods.bootstrap<"hashCode",ClientboundChunkBatchStartPacket,"">(this);
    }

    @Override
    public final boolean equals(Object param0) {
        return ObjectMethods.bootstrap<"equals",ClientboundChunkBatchStartPacket,"">(this, param0);
    }
}

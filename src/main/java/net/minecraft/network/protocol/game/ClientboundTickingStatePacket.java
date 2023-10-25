package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.TickRateManager;

public record ClientboundTickingStatePacket(float tickRate, boolean isFrozen) implements Packet<ClientGamePacketListener> {
    public ClientboundTickingStatePacket(FriendlyByteBuf param0) {
        this(param0.readFloat(), param0.readBoolean());
    }

    public static ClientboundTickingStatePacket from(TickRateManager param0) {
        return new ClientboundTickingStatePacket(param0.tickrate(), param0.isFrozen());
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeFloat(this.tickRate);
        param0.writeBoolean(this.isFrozen);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleTickingState(this);
    }
}

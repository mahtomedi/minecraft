package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ClientboundSetSimulationDistancePacket(int simulationDistance) implements Packet<ClientGamePacketListener> {
    public ClientboundSetSimulationDistancePacket(FriendlyByteBuf param0) {
        this(param0.readVarInt());
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.simulationDistance);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleSetSimulationDistance(this);
    }
}

package net.minecraft.network.protocol.status;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ClientboundStatusResponsePacket(ServerStatus status) implements Packet<ClientStatusPacketListener> {
    public ClientboundStatusResponsePacket(FriendlyByteBuf param0) {
        this(param0.readJsonWithCodec(ServerStatus.CODEC));
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeJsonWithCodec(ServerStatus.CODEC, this.status);
    }

    public void handle(ClientStatusPacketListener param0) {
        param0.handleStatusResponse(this);
    }
}

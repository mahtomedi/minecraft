package net.minecraft.network.protocol.common;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ClientboundResourcePackPopPacket(Optional<UUID> id) implements Packet<ClientCommonPacketListener> {
    public ClientboundResourcePackPopPacket(FriendlyByteBuf param0) {
        this(param0.readOptional(FriendlyByteBuf::readUUID));
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeOptional(this.id, FriendlyByteBuf::writeUUID);
    }

    public void handle(ClientCommonPacketListener param0) {
        param0.handleResourcePackPop(this);
    }
}

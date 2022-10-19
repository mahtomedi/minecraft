package net.minecraft.network.protocol.game;

import java.util.List;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ClientboundPlayerInfoRemovePacket(List<UUID> profileIds) implements Packet<ClientGamePacketListener> {
    public ClientboundPlayerInfoRemovePacket(FriendlyByteBuf param0) {
        this(param0.readList(FriendlyByteBuf::readUUID));
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeCollection(this.profileIds, FriendlyByteBuf::writeUUID);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handlePlayerInfoRemove(this);
    }
}

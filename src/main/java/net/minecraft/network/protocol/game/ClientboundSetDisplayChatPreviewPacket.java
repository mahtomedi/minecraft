package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ClientboundSetDisplayChatPreviewPacket(boolean enabled) implements Packet<ClientGamePacketListener> {
    public ClientboundSetDisplayChatPreviewPacket(FriendlyByteBuf param0) {
        this(param0.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeBoolean(this.enabled);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleSetDisplayChatPreview(this);
    }
}

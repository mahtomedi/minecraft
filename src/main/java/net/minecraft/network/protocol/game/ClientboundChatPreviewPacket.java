package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import org.jetbrains.annotations.Nullable;

public record ClientboundChatPreviewPacket(int queryId, @Nullable Component preview) implements Packet<ClientGamePacketListener> {
    public ClientboundChatPreviewPacket(FriendlyByteBuf param0) {
        this(param0.readInt(), param0.readNullable(FriendlyByteBuf::readComponent));
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeInt(this.queryId);
        param0.writeNullable(this.preview, FriendlyByteBuf::writeComponent);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleChatPreview(this);
    }
}

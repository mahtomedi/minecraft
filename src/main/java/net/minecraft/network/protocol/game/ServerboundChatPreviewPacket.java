package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ServerboundChatPreviewPacket(int queryId, String query) implements Packet<ServerGamePacketListener> {
    public ServerboundChatPreviewPacket(FriendlyByteBuf param0) {
        this(param0.readInt(), param0.readUtf(256));
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeInt(this.queryId);
        param0.writeUtf(this.query, 256);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleChatPreview(this);
    }
}

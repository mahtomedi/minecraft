package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.protocol.Packet;

public record ServerboundChatSessionUpdatePacket(RemoteChatSession.Data chatSession) implements Packet<ServerGamePacketListener> {
    public ServerboundChatSessionUpdatePacket(FriendlyByteBuf param0) {
        this(RemoteChatSession.Data.read(param0));
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        RemoteChatSession.Data.write(param0, this.chatSession);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleChatSessionUpdate(this);
    }
}

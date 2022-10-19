package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public record ClientboundDisguisedChatPacket(Component message, ChatType.BoundNetwork chatType) implements Packet<ClientGamePacketListener> {
    public ClientboundDisguisedChatPacket(FriendlyByteBuf param0) {
        this(param0.readComponent(), new ChatType.BoundNetwork(param0));
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeComponent(this.message);
        this.chatType.write(param0);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleDisguisedChat(this);
    }

    @Override
    public boolean isSkippable() {
        return true;
    }
}

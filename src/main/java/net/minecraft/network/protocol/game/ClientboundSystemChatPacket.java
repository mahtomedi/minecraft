package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public record ClientboundSystemChatPacket(Component content, ChatType type) implements Packet<ClientGamePacketListener> {
    public ClientboundSystemChatPacket(FriendlyByteBuf param0) {
        this(param0.readComponent(), ChatType.getForIndex(param0.readByte()));
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeComponent(this.content);
        param0.writeByte(this.type.getIndex());
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleSystemChat(this);
    }

    @Override
    public boolean isSkippable() {
        return true;
    }
}

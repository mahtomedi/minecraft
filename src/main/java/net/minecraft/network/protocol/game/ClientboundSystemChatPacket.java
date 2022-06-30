package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public record ClientboundSystemChatPacket(Component content, boolean overlay) implements Packet<ClientGamePacketListener> {
    public ClientboundSystemChatPacket(FriendlyByteBuf param0) {
        this(param0.readComponent(), param0.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeComponent(this.content);
        param0.writeBoolean(this.overlay);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleSystemChat(this);
    }

    @Override
    public boolean isSkippable() {
        return true;
    }
}

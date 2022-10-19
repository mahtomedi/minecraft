package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.protocol.Packet;

public record ClientboundDeleteChatPacket(MessageSignature.Packed messageSignature) implements Packet<ClientGamePacketListener> {
    public ClientboundDeleteChatPacket(FriendlyByteBuf param0) {
        this(MessageSignature.Packed.read(param0));
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        MessageSignature.Packed.write(param0, this.messageSignature);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleDeleteChat(this);
    }
}

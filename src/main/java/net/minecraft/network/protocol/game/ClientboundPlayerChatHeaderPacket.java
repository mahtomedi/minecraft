package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.SignedMessageHeader;
import net.minecraft.network.protocol.Packet;

public record ClientboundPlayerChatHeaderPacket(SignedMessageHeader header, MessageSignature headerSignature, byte[] bodyDigest)
    implements Packet<ClientGamePacketListener> {
    public ClientboundPlayerChatHeaderPacket(FriendlyByteBuf param0) {
        this(new SignedMessageHeader(param0), new MessageSignature(param0), param0.readByteArray());
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        this.header.write(param0);
        this.headerSignature.write(param0);
        param0.writeByteArray(this.bodyDigest);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handlePlayerChatHeader(this);
    }
}

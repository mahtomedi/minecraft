package net.minecraft.network.protocol.game;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FilterMask;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.protocol.Packet;

public record ClientboundPlayerChatPacket(
    UUID sender,
    int index,
    @Nullable MessageSignature signature,
    SignedMessageBody.Packed body,
    @Nullable Component unsignedContent,
    FilterMask filterMask,
    ChatType.BoundNetwork chatType
) implements Packet<ClientGamePacketListener> {
    public ClientboundPlayerChatPacket(FriendlyByteBuf param0) {
        this(
            param0.readUUID(),
            param0.readVarInt(),
            param0.readNullable(MessageSignature::read),
            new SignedMessageBody.Packed(param0),
            param0.readNullable(FriendlyByteBuf::readComponent),
            FilterMask.read(param0),
            new ChatType.BoundNetwork(param0)
        );
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeUUID(this.sender);
        param0.writeVarInt(this.index);
        param0.writeNullable(this.signature, MessageSignature::write);
        this.body.write(param0);
        param0.writeNullable(this.unsignedContent, FriendlyByteBuf::writeComponent);
        FilterMask.write(param0, this.filterMask);
        this.chatType.write(param0);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handlePlayerChat(this);
    }

    @Override
    public boolean isSkippable() {
        return true;
    }
}

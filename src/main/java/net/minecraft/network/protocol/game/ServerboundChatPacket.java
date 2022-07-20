package net.minecraft.network.protocol.game;

import java.time.Instant;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MessageSigner;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;

public record ServerboundChatPacket(
    String message, Instant timeStamp, long salt, MessageSignature signature, boolean signedPreview, LastSeenMessages.Update lastSeenMessages
) implements Packet<ServerGamePacketListener> {
    public ServerboundChatPacket(FriendlyByteBuf param0) {
        this(
            param0.readUtf(256),
            param0.readInstant(),
            param0.readLong(),
            new MessageSignature(param0),
            param0.readBoolean(),
            new LastSeenMessages.Update(param0)
        );
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeUtf(this.message, 256);
        param0.writeInstant(this.timeStamp);
        param0.writeLong(this.salt);
        this.signature.write(param0);
        param0.writeBoolean(this.signedPreview);
        this.lastSeenMessages.write(param0);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleChat(this);
    }

    public MessageSigner getSigner(ServerPlayer param0) {
        return new MessageSigner(param0.getUUID(), this.timeStamp, this.salt);
    }
}

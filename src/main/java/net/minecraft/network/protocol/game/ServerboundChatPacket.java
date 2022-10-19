package net.minecraft.network.protocol.game;

import java.time.Instant;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.protocol.Packet;

public record ServerboundChatPacket(String message, Instant timeStamp, long salt, @Nullable MessageSignature signature, LastSeenMessages.Update lastSeenMessages)
    implements Packet<ServerGamePacketListener> {
    public ServerboundChatPacket(FriendlyByteBuf param0) {
        this(param0.readUtf(256), param0.readInstant(), param0.readLong(), param0.readNullable(MessageSignature::read), new LastSeenMessages.Update(param0));
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeUtf(this.message, 256);
        param0.writeInstant(this.timeStamp);
        param0.writeLong(this.salt);
        param0.writeNullable(this.signature, MessageSignature::write);
        this.lastSeenMessages.write(param0);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleChat(this);
    }
}

package net.minecraft.network.protocol.game;

import java.time.Instant;
import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.protocol.Packet;

public record ServerboundChatCommandPacket(
    String command, Instant timeStamp, long salt, ArgumentSignatures argumentSignatures, LastSeenMessages.Update lastSeenMessages
) implements Packet<ServerGamePacketListener> {
    public ServerboundChatCommandPacket(FriendlyByteBuf param0) {
        this(param0.readUtf(256), param0.readInstant(), param0.readLong(), new ArgumentSignatures(param0), new LastSeenMessages.Update(param0));
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeUtf(this.command, 256);
        param0.writeInstant(this.timeStamp);
        param0.writeLong(this.salt);
        this.argumentSignatures.write(param0);
        this.lastSeenMessages.write(param0);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleChatCommand(this);
    }
}

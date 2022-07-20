package net.minecraft.network.protocol.game;

import java.time.Instant;
import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.StringUtil;

public record ServerboundChatCommandPacket(
    String command, Instant timeStamp, long salt, ArgumentSignatures argumentSignatures, boolean signedPreview, LastSeenMessages.Update lastSeenMessages
) implements Packet<ServerGamePacketListener> {
    public ServerboundChatCommandPacket(String param0, Instant param1, long param2, ArgumentSignatures param3, boolean param4, LastSeenMessages.Update param5) {
        param0 = StringUtil.trimChatMessage(param0);
        this.command = param0;
        this.timeStamp = param1;
        this.salt = param2;
        this.argumentSignatures = param3;
        this.signedPreview = param4;
        this.lastSeenMessages = param5;
    }

    public ServerboundChatCommandPacket(FriendlyByteBuf param0) {
        this(
            param0.readUtf(256),
            param0.readInstant(),
            param0.readLong(),
            new ArgumentSignatures(param0),
            param0.readBoolean(),
            new LastSeenMessages.Update(param0)
        );
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeUtf(this.command, 256);
        param0.writeInstant(this.timeStamp);
        param0.writeLong(this.salt);
        this.argumentSignatures.write(param0);
        param0.writeBoolean(this.signedPreview);
        this.lastSeenMessages.write(param0);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleChatCommand(this);
    }
}

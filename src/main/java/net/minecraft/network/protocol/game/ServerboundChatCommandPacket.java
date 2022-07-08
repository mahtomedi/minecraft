package net.minecraft.network.protocol.game;

import java.time.Instant;
import net.minecraft.commands.CommandSigningContext;
import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MessageSigner;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringUtil;

public record ServerboundChatCommandPacket(String command, Instant timeStamp, long salt, ArgumentSignatures argumentSignatures, boolean signedPreview)
    implements Packet<ServerGamePacketListener> {
    public ServerboundChatCommandPacket(String param0, Instant param1, long param2, ArgumentSignatures param3, boolean param4) {
        param0 = StringUtil.trimChatMessage(param0);
        this.command = param0;
        this.timeStamp = param1;
        this.salt = param2;
        this.argumentSignatures = param3;
        this.signedPreview = param4;
    }

    public ServerboundChatCommandPacket(FriendlyByteBuf param0) {
        this(param0.readUtf(256), param0.readInstant(), param0.readLong(), new ArgumentSignatures(param0), param0.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeUtf(this.command, 256);
        param0.writeInstant(this.timeStamp);
        param0.writeLong(this.salt);
        this.argumentSignatures.write(param0);
        param0.writeBoolean(this.signedPreview);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleChatCommand(this);
    }

    public CommandSigningContext signingContext(ServerPlayer param0) {
        MessageSigner var0 = new MessageSigner(param0.getUUID(), this.timeStamp, this.salt);
        return new CommandSigningContext.SignedArguments(param0.connection.signedMessageDecoder(), var0, this.argumentSignatures, this.signedPreview);
    }
}

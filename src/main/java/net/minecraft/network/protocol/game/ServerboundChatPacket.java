package net.minecraft.network.protocol.game;

import java.time.Instant;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MessageSigner;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringUtil;

public record ServerboundChatPacket(String message, Instant timeStamp, long salt, MessageSignature signature, boolean signedPreview)
    implements Packet<ServerGamePacketListener> {
    public ServerboundChatPacket(String param0, Instant param1, long param2, MessageSignature param3, boolean param4) {
        param0 = StringUtil.trimChatMessage(param0);
        this.message = param0;
        this.timeStamp = param1;
        this.salt = param2;
        this.signature = param3;
        this.signedPreview = param4;
    }

    public ServerboundChatPacket(FriendlyByteBuf param0) {
        this(param0.readUtf(256), param0.readInstant(), param0.readLong(), new MessageSignature(param0), param0.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeUtf(this.message, 256);
        param0.writeInstant(this.timeStamp);
        param0.writeLong(this.salt);
        this.signature.write(param0);
        param0.writeBoolean(this.signedPreview);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleChat(this);
    }

    public MessageSigner getSigner(ServerPlayer param0) {
        return new MessageSigner(param0.getUUID(), this.timeStamp, this.salt);
    }
}

package net.minecraft.network.protocol.game;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Crypt;
import net.minecraft.util.StringUtil;

public class ServerboundChatPacket implements Packet<ServerGamePacketListener> {
    public static final Duration MESSAGE_EXPIRES_AFTER = Duration.ofMinutes(5L);
    private final String message;
    private final Instant timeStamp;
    private final Crypt.SaltSignaturePair saltSignature;
    private final boolean signedPreview;

    public ServerboundChatPacket(String param0, MessageSignature param1, boolean param2) {
        this.message = StringUtil.trimChatMessage(param0);
        this.timeStamp = param1.timeStamp();
        this.saltSignature = param1.saltSignature();
        this.signedPreview = param2;
    }

    public ServerboundChatPacket(FriendlyByteBuf param0) {
        this.message = param0.readUtf(256);
        this.timeStamp = param0.readInstant();
        this.saltSignature = new Crypt.SaltSignaturePair(param0);
        this.signedPreview = param0.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeUtf(this.message, 256);
        param0.writeInstant(this.timeStamp);
        Crypt.SaltSignaturePair.write(param0, this.saltSignature);
        param0.writeBoolean(this.signedPreview);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleChat(this);
    }

    public String getMessage() {
        return this.message;
    }

    public MessageSignature getSignature(UUID param0) {
        return new MessageSignature(param0, this.timeStamp, this.saltSignature);
    }

    public Instant getTimeStamp() {
        return this.timeStamp;
    }

    public boolean signedPreview() {
        return this.signedPreview;
    }
}

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
    public static final Duration MESSAGE_EXPIRES_AFTER = Duration.ofMinutes(2L);
    private final String message;
    private final Instant timeStamp;
    private final Crypt.SaltSignaturePair saltSignature;

    public ServerboundChatPacket(String param0, MessageSignature param1) {
        this.message = StringUtil.trimChatMessage(param0);
        this.timeStamp = param1.timeStamp();
        this.saltSignature = param1.saltSignature();
    }

    public ServerboundChatPacket(FriendlyByteBuf param0) {
        this.message = param0.readUtf(256);
        this.timeStamp = Instant.ofEpochSecond(param0.readLong());
        this.saltSignature = new Crypt.SaltSignaturePair(param0);
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeUtf(this.message);
        param0.writeLong(this.timeStamp.getEpochSecond());
        this.saltSignature.write(param0);
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

    private Instant getExpiresAt() {
        return this.timeStamp.plus(MESSAGE_EXPIRES_AFTER);
    }

    public boolean hasExpired(Instant param0) {
        return param0.isAfter(this.getExpiresAt());
    }
}

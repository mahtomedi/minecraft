package net.minecraft.network.protocol.game;

import java.time.Duration;
import java.time.Instant;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Crypt;
import org.apache.commons.lang3.StringUtils;

public class ServerboundChatPacket implements Packet<ServerGamePacketListener> {
    private static final int MAX_MESSAGE_LENGTH = 256;
    public static final Duration MESSAGE_EXPIRES_AFTER = Duration.ofMinutes(2L);
    private final Instant timeStamp;
    private final String message;
    private final Crypt.SaltSignaturePair saltSignature;

    public ServerboundChatPacket(Instant param0, String param1, Crypt.SaltSignaturePair param2) {
        this.timeStamp = param0;
        this.message = trimMessage(param1);
        this.saltSignature = param2;
    }

    public ServerboundChatPacket(FriendlyByteBuf param0) {
        this.timeStamp = Instant.ofEpochSecond(param0.readLong());
        this.message = param0.readUtf(256);
        this.saltSignature = new Crypt.SaltSignaturePair(param0);
    }

    private static String trimMessage(String param0) {
        return param0.length() > 256 ? param0.substring(0, 256) : param0;
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeLong(this.timeStamp.getEpochSecond());
        param0.writeUtf(this.message);
        this.saltSignature.write(param0);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleChat(this);
    }

    public Instant getTimeStamp() {
        return this.timeStamp;
    }

    public String getMessage() {
        return this.message;
    }

    public String getMessageNormalized() {
        return StringUtils.normalizeSpace(this.message);
    }

    public Crypt.SaltSignaturePair getSaltSignature() {
        return this.saltSignature;
    }

    private Instant getExpiresAt() {
        return this.timeStamp.plus(MESSAGE_EXPIRES_AFTER);
    }

    public boolean hasExpired(Instant param0) {
        return param0.isAfter(this.getExpiresAt());
    }
}

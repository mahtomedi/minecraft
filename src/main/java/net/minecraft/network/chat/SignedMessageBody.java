package net.minecraft.network.chat;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.time.Instant;
import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.SignatureUpdater;

public record SignedMessageBody(String content, Instant timeStamp, long salt, LastSeenMessages lastSeen) {
    public static final MapCodec<SignedMessageBody> MAP_CODEC = RecordCodecBuilder.mapCodec(
        param0 -> param0.group(
                    Codec.STRING.fieldOf("content").forGetter(SignedMessageBody::content),
                    ExtraCodecs.INSTANT_ISO8601.fieldOf("time_stamp").forGetter(SignedMessageBody::timeStamp),
                    Codec.LONG.fieldOf("salt").forGetter(SignedMessageBody::salt),
                    LastSeenMessages.CODEC.optionalFieldOf("last_seen", LastSeenMessages.EMPTY).forGetter(SignedMessageBody::lastSeen)
                )
                .apply(param0, SignedMessageBody::new)
    );

    public static SignedMessageBody unsigned(String param0) {
        return new SignedMessageBody(param0, Instant.now(), 0L, LastSeenMessages.EMPTY);
    }

    public void updateSignature(SignatureUpdater.Output param0) throws SignatureException {
        param0.update(Longs.toByteArray(this.salt));
        param0.update(Longs.toByteArray(this.timeStamp.getEpochSecond()));
        byte[] var0 = this.content.getBytes(StandardCharsets.UTF_8);
        param0.update(Ints.toByteArray(var0.length));
        param0.update(var0);
        this.lastSeen.updateSignature(param0);
    }

    public SignedMessageBody.Packed pack(MessageSignatureCache param0) {
        return new SignedMessageBody.Packed(this.content, this.timeStamp, this.salt, this.lastSeen.pack(param0));
    }

    public static record Packed(String content, Instant timeStamp, long salt, LastSeenMessages.Packed lastSeen) {
        public Packed(FriendlyByteBuf param0) {
            this(param0.readUtf(256), param0.readInstant(), param0.readLong(), new LastSeenMessages.Packed(param0));
        }

        public void write(FriendlyByteBuf param0) {
            param0.writeUtf(this.content, 256);
            param0.writeInstant(this.timeStamp);
            param0.writeLong(this.salt);
            this.lastSeen.write(param0);
        }

        public Optional<SignedMessageBody> unpack(MessageSignatureCache param0) {
            return this.lastSeen.unpack(param0).map(param0x -> new SignedMessageBody(this.content, this.timeStamp, this.salt, param0x));
        }
    }
}

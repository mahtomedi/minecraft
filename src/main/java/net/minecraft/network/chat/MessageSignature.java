package net.minecraft.network.chat;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.SignatureUpdater;
import net.minecraft.util.SignatureValidator;

public record MessageSignature(byte[] bytes) {
    public static final Codec<MessageSignature> CODEC = ExtraCodecs.BASE64_STRING.xmap(MessageSignature::new, MessageSignature::bytes);
    public static final int BYTES = 256;

    public MessageSignature(byte[] param0) {
        Preconditions.checkState(param0.length == 256, "Invalid message signature size");
        this.bytes = param0;
    }

    public static MessageSignature read(FriendlyByteBuf param0) {
        byte[] var0 = new byte[256];
        param0.readBytes(var0);
        return new MessageSignature(var0);
    }

    public static void write(FriendlyByteBuf param0, MessageSignature param1) {
        param0.writeBytes(param1.bytes);
    }

    public boolean verify(SignatureValidator param0, SignatureUpdater param1) {
        return param0.validate(param1, this.bytes);
    }

    public ByteBuffer asByteBuffer() {
        return ByteBuffer.wrap(this.bytes);
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else {
            if (param0 instanceof MessageSignature var0 && Arrays.equals(this.bytes, var0.bytes)) {
                return true;
            }

            return false;
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.bytes);
    }

    @Override
    public String toString() {
        return Base64.getEncoder().encodeToString(this.bytes);
    }

    public MessageSignature.Packed pack(MessageSignatureCache param0) {
        int var0 = param0.pack(this);
        return var0 != -1 ? new MessageSignature.Packed(var0) : new MessageSignature.Packed(this);
    }

    public static record Packed(int id, @Nullable MessageSignature fullSignature) {
        public static final int FULL_SIGNATURE = -1;

        public Packed(MessageSignature param0) {
            this(-1, param0);
        }

        public Packed(int param0) {
            this(param0, null);
        }

        public static MessageSignature.Packed read(FriendlyByteBuf param0) {
            int var0 = param0.readVarInt() - 1;
            return var0 == -1 ? new MessageSignature.Packed(MessageSignature.read(param0)) : new MessageSignature.Packed(var0);
        }

        public static void write(FriendlyByteBuf param0, MessageSignature.Packed param1) {
            param0.writeVarInt(param1.id() + 1);
            if (param1.fullSignature() != null) {
                MessageSignature.write(param0, param1.fullSignature());
            }

        }

        public Optional<MessageSignature> unpack(MessageSignatureCache param0) {
            return this.fullSignature != null ? Optional.of(this.fullSignature) : Optional.ofNullable(param0.unpack(this.id));
        }
    }
}

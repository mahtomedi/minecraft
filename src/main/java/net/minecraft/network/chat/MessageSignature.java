package net.minecraft.network.chat;

import it.unimi.dsi.fastutil.bytes.ByteArrays;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Base64;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.SignatureUpdater;
import net.minecraft.util.SignatureValidator;

public record MessageSignature(byte[] bytes) {
    public static final MessageSignature EMPTY = new MessageSignature(ByteArrays.EMPTY_ARRAY);

    public MessageSignature(FriendlyByteBuf param0) {
        this(param0.readByteArray());
    }

    public void write(FriendlyByteBuf param0) {
        param0.writeByteArray(this.bytes);
    }

    public boolean verify(SignatureValidator param0, SignedMessageHeader param1, SignedMessageBody param2) {
        if (!this.isEmpty()) {
            byte[] var0 = param2.hash().asBytes();
            return param0.validate((SignatureUpdater)(param2x -> param1.updateSignature(param2x, var0)), this.bytes);
        } else {
            return false;
        }
    }

    public boolean verify(SignatureValidator param0, SignedMessageHeader param1, byte[] param2) {
        return !this.isEmpty() ? param0.validate((SignatureUpdater)(param2x -> param1.updateSignature(param2x, param2)), this.bytes) : false;
    }

    public boolean isEmpty() {
        return this.bytes.length == 0;
    }

    @Nullable
    public ByteBuffer asByteBuffer() {
        return !this.isEmpty() ? ByteBuffer.wrap(this.bytes) : null;
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
        return !this.isEmpty() ? Base64.getEncoder().encodeToString(this.bytes) : "empty";
    }
}

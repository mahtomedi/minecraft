package net.minecraft.network.chat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.time.Instant;
import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.util.Crypt;
import net.minecraft.util.SignatureUpdater;
import net.minecraft.util.SignatureValidator;

public record MessageSignature(UUID sender, Instant timeStamp, Crypt.SaltSignaturePair saltSignature) {
    public static MessageSignature unsigned() {
        return new MessageSignature(Util.NIL_UUID, Instant.now(), Crypt.SaltSignaturePair.EMPTY);
    }

    public boolean verify(SignatureValidator param0, Component param1) {
        return this.isValid()
            ? param0.validate(
                (SignatureUpdater)(param1x -> updateSignature(param1x, param1, this.sender, this.timeStamp, this.saltSignature.salt())),
                this.saltSignature.signature()
            )
            : false;
    }

    public boolean verify(SignatureValidator param0, String param1) throws SignatureException {
        return this.verify(param0, Component.literal(param1));
    }

    public static void updateSignature(SignatureUpdater.Output param0, Component param1, UUID param2, Instant param3, long param4) throws SignatureException {
        byte[] var0 = new byte[32];
        ByteBuffer var1 = ByteBuffer.wrap(var0).order(ByteOrder.BIG_ENDIAN);
        var1.putLong(param4);
        var1.putLong(param2.getMostSignificantBits()).putLong(param2.getLeastSignificantBits());
        var1.putLong(param3.getEpochSecond());
        param0.update(var0);
        param0.update(encodeContent(param1));
    }

    private static byte[] encodeContent(Component param0) {
        String var0 = Component.Serializer.toStableJson(param0);
        return var0.getBytes(StandardCharsets.UTF_8);
    }

    public boolean isValid() {
        return this.sender != Util.NIL_UUID && this.saltSignature.isValid();
    }

    public boolean isValid(UUID param0) {
        return this.isValid() && param0.equals(this.sender);
    }
}

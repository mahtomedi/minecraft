package net.minecraft.network.chat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.Signature;
import java.security.SignatureException;
import java.time.Instant;
import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.util.Crypt;

public record MessageSignature(UUID sender, Instant timeStamp, Crypt.SaltSignaturePair saltSignature) {
    public static MessageSignature unsigned() {
        return new MessageSignature(Util.NIL_UUID, Instant.now(), Crypt.SaltSignaturePair.EMPTY);
    }

    public boolean verify(Signature param0, Component param1) throws SignatureException {
        updateSignature(param0, param1, this.sender, this.timeStamp, this.saltSignature.salt());
        return param0.verify(this.saltSignature.signature());
    }

    public boolean verify(Signature param0, String param1) throws SignatureException {
        return this.verify(param0, Component.literal(param1));
    }

    public static void updateSignature(Signature param0, Component param1, UUID param2, Instant param3, long param4) throws SignatureException {
        byte[] var0 = encodeContent(param1);
        int var1 = 32 + var0.length;
        ByteBuffer var2 = ByteBuffer.allocate(var1).order(ByteOrder.BIG_ENDIAN);
        var2.putLong(param4);
        var2.putLong(param2.getMostSignificantBits()).putLong(param2.getLeastSignificantBits());
        var2.putLong(param3.getEpochSecond());
        var2.put(var0);
        param0.update(var2.flip());
    }

    private static byte[] encodeContent(Component param0) {
        String var0 = Component.Serializer.toStableJson(param0);
        return var0.getBytes(StandardCharsets.UTF_8);
    }
}

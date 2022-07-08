package net.minecraft.network.chat;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;

public record SignedMessageBody(Component content, Instant timeStamp, long salt, List<SignedMessageBody.LastSeen> lastSeen) {
    private static final byte HASH_SEPARATOR_BYTE = 70;

    public SignedMessageBody(FriendlyByteBuf param0) {
        this(param0.readComponent(), param0.readInstant(), param0.readLong(), param0.readCollection(ArrayList::new, SignedMessageBody.LastSeen::new));
    }

    public void write(FriendlyByteBuf param0) {
        param0.writeComponent(this.content);
        param0.writeInstant(this.timeStamp);
        param0.writeLong(this.salt);
        param0.writeCollection(this.lastSeen, (param0x, param1) -> param1.write(param0x));
    }

    public HashCode hash() {
        byte[] var0 = encodeContent(this.content);
        byte[] var1 = encodeLastSeen(this.lastSeen);
        byte[] var2 = new byte[16 + var0.length];
        ByteBuffer var3 = ByteBuffer.wrap(var2).order(ByteOrder.BIG_ENDIAN);
        var3.putLong(this.salt);
        var3.putLong(this.timeStamp.getEpochSecond());
        var3.put(var0);
        var3.put(var1);
        return Hashing.sha256().hashBytes(var2);
    }

    private static byte[] encodeContent(Component param0) {
        String var0 = Component.Serializer.toStableJson(param0);
        return var0.getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] encodeLastSeen(List<SignedMessageBody.LastSeen> param0) {
        int var0 = param0.stream().mapToInt(param0x -> 17 + param0x.lastSignature().bytes().length).sum();
        byte[] var1 = new byte[var0];
        ByteBuffer var2 = ByteBuffer.wrap(var1).order(ByteOrder.BIG_ENDIAN);

        for(SignedMessageBody.LastSeen var3 : param0) {
            UUID var4 = var3.profileId();
            MessageSignature var5 = var3.lastSignature();
            var2.put((byte)70).putLong(var4.getMostSignificantBits()).putLong(var4.getLeastSignificantBits()).put(var5.bytes());
        }

        return var1;
    }

    public static record LastSeen(UUID profileId, MessageSignature lastSignature) {
        public LastSeen(FriendlyByteBuf param0) {
            this(param0.readUUID(), new MessageSignature(param0));
        }

        public void write(FriendlyByteBuf param0) {
            param0.writeUUID(this.profileId);
            this.lastSignature.write(param0);
        }
    }
}

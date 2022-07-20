package net.minecraft.network.chat;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import net.minecraft.network.FriendlyByteBuf;

public record SignedMessageBody(ChatMessageContent content, Instant timeStamp, long salt, LastSeenMessages lastSeen) {
    public static final byte HASH_SEPARATOR_BYTE = 70;

    public SignedMessageBody(FriendlyByteBuf param0) {
        this(ChatMessageContent.read(param0), param0.readInstant(), param0.readLong(), new LastSeenMessages(param0));
    }

    public void write(FriendlyByteBuf param0) {
        ChatMessageContent.write(param0, this.content);
        param0.writeInstant(this.timeStamp);
        param0.writeLong(this.salt);
        this.lastSeen.write(param0);
    }

    public HashCode hash() {
        HashingOutputStream var0 = new HashingOutputStream(Hashing.sha256(), OutputStream.nullOutputStream());

        try {
            DataOutputStream var1 = new DataOutputStream(var0);
            var1.writeLong(this.salt);
            var1.writeLong(this.timeStamp.getEpochSecond());
            OutputStreamWriter var2 = new OutputStreamWriter(var1, StandardCharsets.UTF_8);
            var2.write(this.content.plain());
            var2.flush();
            var1.write(70);
            if (this.content.isDecorated()) {
                var2.write(Component.Serializer.toStableJson(this.content.decorated()));
                var2.flush();
            }

            this.lastSeen.updateHash(var1);
        } catch (IOException var4) {
        }

        return var0.hash();
    }

    public SignedMessageBody withContent(ChatMessageContent param0) {
        return new SignedMessageBody(param0, this.timeStamp, this.salt, this.lastSeen);
    }
}

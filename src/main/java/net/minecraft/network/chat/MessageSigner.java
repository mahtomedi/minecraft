package net.minecraft.network.chat;

import java.time.Instant;
import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Crypt;

public record MessageSigner(UUID profileId, Instant timeStamp, long salt) {
    public MessageSigner(FriendlyByteBuf param0) {
        this(param0.readUUID(), param0.readInstant(), param0.readLong());
    }

    public static MessageSigner create(UUID param0) {
        return new MessageSigner(param0, Instant.now(), Crypt.SaltSupplier.getLong());
    }

    public static MessageSigner system() {
        return create(Util.NIL_UUID);
    }

    public void write(FriendlyByteBuf param0) {
        param0.writeUUID(this.profileId);
        param0.writeInstant(this.timeStamp);
        param0.writeLong(this.salt);
    }

    public boolean isSystem() {
        return this.profileId.equals(Util.NIL_UUID);
    }
}

package net.minecraft.network.chat;

import java.security.Signature;
import java.security.SignatureException;
import java.time.Instant;
import java.util.UUID;
import net.minecraft.util.Crypt;

public record MessageSigner(UUID sender, Instant timeStamp, long salt) {
    public static MessageSigner create(UUID param0) {
        return new MessageSigner(param0, Instant.now(), Crypt.SaltSupplier.getLong());
    }

    public MessageSignature sign(Signature param0, Component param1) throws SignatureException {
        MessageSignature.updateSignature(param0, param1, this.sender, this.timeStamp, this.salt);
        return new MessageSignature(this.sender, this.timeStamp, new Crypt.SaltSignaturePair(this.salt, param0.sign()));
    }

    public MessageSignature sign(Signature param0, String param1) throws SignatureException {
        return this.sign(param0, Component.literal(param1));
    }
}

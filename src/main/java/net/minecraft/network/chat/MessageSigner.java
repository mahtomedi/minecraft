package net.minecraft.network.chat;

import java.security.SignatureException;
import java.time.Instant;
import java.util.UUID;
import net.minecraft.util.Crypt;
import net.minecraft.util.SignatureUpdater;
import net.minecraft.util.Signer;

public record MessageSigner(UUID sender, Instant timeStamp, long salt) {
    public static MessageSigner create(UUID param0) {
        return new MessageSigner(param0, Instant.now(), Crypt.SaltSupplier.getLong());
    }

    public MessageSignature sign(Signer param0, Component param1) {
        byte[] var0 = param0.sign((SignatureUpdater)(param1x -> MessageSignature.updateSignature(param1x, param1, this.sender, this.timeStamp, this.salt)));
        return new MessageSignature(this.sender, this.timeStamp, new Crypt.SaltSignaturePair(this.salt, var0));
    }

    public MessageSignature sign(Signer param0, String param1) throws SignatureException {
        return this.sign(param0, Component.literal(param1));
    }
}

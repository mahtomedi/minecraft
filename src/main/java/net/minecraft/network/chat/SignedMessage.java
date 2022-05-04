package net.minecraft.network.chat;

import java.security.GeneralSecurityException;
import java.security.Signature;
import java.security.SignatureException;
import net.minecraft.util.CryptException;
import net.minecraft.world.entity.player.ProfilePublicKey;

public record SignedMessage(Component content, MessageSignature signature) {
    public SignedMessage(String param0, MessageSignature param1) {
        this(Component.literal(param0), param1);
    }

    public boolean verify(Signature param0) throws SignatureException {
        return this.signature.verify(param0, this.content);
    }

    public boolean verify(ProfilePublicKey param0) {
        try {
            return this.verify(param0.verifySignature());
        } catch (CryptException | GeneralSecurityException var3) {
            return false;
        }
    }
}

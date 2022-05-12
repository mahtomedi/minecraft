package net.minecraft.network.chat;

import java.security.GeneralSecurityException;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Optional;
import net.minecraft.util.CryptException;
import net.minecraft.world.entity.player.ProfilePublicKey;

public record PlayerChatMessage(Component signedContent, MessageSignature signature, Optional<Component> unsignedContent) {
    public static PlayerChatMessage signed(Component param0, MessageSignature param1) {
        return new PlayerChatMessage(param0, param1, Optional.empty());
    }

    public static PlayerChatMessage signed(String param0, MessageSignature param1) {
        return signed(Component.literal(param0), param1);
    }

    public static PlayerChatMessage unsigned(Component param0) {
        return new PlayerChatMessage(param0, MessageSignature.unsigned(), Optional.empty());
    }

    public PlayerChatMessage withUnsignedContent(Component param0) {
        return new PlayerChatMessage(this.signedContent, this.signature, Optional.of(param0));
    }

    public boolean verify(Signature param0) throws SignatureException {
        return this.signature.verify(param0, this.signedContent);
    }

    public boolean verify(ProfilePublicKey param0) {
        if (!this.signature.isValid()) {
            return false;
        } else {
            try {
                return this.verify(param0.verifySignature());
            } catch (CryptException | GeneralSecurityException var3) {
                return false;
            }
        }
    }

    public Component serverContent() {
        return this.unsignedContent.orElse(this.signedContent);
    }
}

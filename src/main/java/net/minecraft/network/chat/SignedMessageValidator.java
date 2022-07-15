package net.minecraft.network.chat;

import javax.annotation.Nullable;
import net.minecraft.util.SignatureValidator;
import net.minecraft.world.entity.player.ProfilePublicKey;

public interface SignedMessageValidator {
    SignedMessageValidator ALWAYS_REJECT = new SignedMessageValidator() {
        @Override
        public void validateHeader(SignedMessageHeader param0, MessageSignature param1, byte[] param2) {
        }

        @Override
        public boolean validateMessage(PlayerChatMessage param0) {
            return false;
        }
    };

    static SignedMessageValidator create(@Nullable ProfilePublicKey param0) {
        return (SignedMessageValidator)(param0 != null ? new SignedMessageValidator.KeyBased(param0.createSignatureValidator()) : ALWAYS_REJECT);
    }

    void validateHeader(SignedMessageHeader var1, MessageSignature var2, byte[] var3);

    boolean validateMessage(PlayerChatMessage var1);

    public static class KeyBased implements SignedMessageValidator {
        private final SignatureValidator validator;
        @Nullable
        private MessageSignature lastSignature;
        boolean isChainConsistent = true;

        public KeyBased(SignatureValidator param0) {
            this.validator = param0;
        }

        private boolean validateChain(SignedMessageHeader param0, MessageSignature param1) {
            return this.lastSignature == null || this.lastSignature.equals(param0.previousSignature()) || this.lastSignature.equals(param1);
        }

        private boolean validateContents(SignedMessageHeader param0, MessageSignature param1, byte[] param2) {
            return this.validateChain(param0, param1) && param1.verify(this.validator, param0, param2);
        }

        @Override
        public void validateHeader(SignedMessageHeader param0, MessageSignature param1, byte[] param2) {
            this.isChainConsistent = this.isChainConsistent && this.validateContents(param0, param1, param2);
            this.lastSignature = param1;
        }

        @Override
        public boolean validateMessage(PlayerChatMessage param0) {
            if (this.isChainConsistent && this.validateContents(param0.signedHeader(), param0.headerSignature(), param0.signedBody().hash().asBytes())) {
                this.lastSignature = param0.headerSignature();
                return true;
            } else {
                this.isChainConsistent = true;
                this.lastSignature = null;
                return false;
            }
        }
    }
}

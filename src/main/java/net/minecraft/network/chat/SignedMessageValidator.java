package net.minecraft.network.chat;

import javax.annotation.Nullable;
import net.minecraft.util.SignatureValidator;
import net.minecraft.world.entity.player.ProfilePublicKey;

public interface SignedMessageValidator {
    static SignedMessageValidator alwaysReturn(final SignedMessageValidator.State param0) {
        return new SignedMessageValidator() {
            @Override
            public SignedMessageValidator.State validateHeader(SignedMessageHeader param0x, MessageSignature param1, byte[] param2) {
                return param0;
            }

            @Override
            public SignedMessageValidator.State validateMessage(PlayerChatMessage param0x) {
                return param0;
            }
        };
    }

    static SignedMessageValidator create(@Nullable ProfilePublicKey param0, boolean param1) {
        return (SignedMessageValidator)(param0 == null
            ? alwaysReturn(param1 ? SignedMessageValidator.State.BROKEN_CHAIN : SignedMessageValidator.State.NOT_SECURE)
            : new SignedMessageValidator.KeyBased(param0.createSignatureValidator()));
    }

    SignedMessageValidator.State validateHeader(SignedMessageHeader var1, MessageSignature var2, byte[] var3);

    SignedMessageValidator.State validateMessage(PlayerChatMessage var1);

    public static class KeyBased implements SignedMessageValidator {
        private final SignatureValidator validator;
        @Nullable
        private MessageSignature lastSignature;
        private boolean isChainConsistent = true;

        public KeyBased(SignatureValidator param0) {
            this.validator = param0;
        }

        private boolean validateChain(SignedMessageHeader param0, MessageSignature param1) {
            if (param1.isEmpty()) {
                return false;
            } else {
                return this.lastSignature == null || this.lastSignature.equals(param0.previousSignature()) || this.lastSignature.equals(param1);
            }
        }

        private boolean validateContents(SignedMessageHeader param0, MessageSignature param1, byte[] param2) {
            return param1.verify(this.validator, param0, param2);
        }

        private SignedMessageValidator.State updateAndValidate(SignedMessageHeader param0, MessageSignature param1, byte[] param2) {
            this.isChainConsistent = this.isChainConsistent && this.validateChain(param0, param1);
            if (!this.isChainConsistent) {
                return SignedMessageValidator.State.BROKEN_CHAIN;
            } else if (!this.validateContents(param0, param1, param2)) {
                this.lastSignature = null;
                return SignedMessageValidator.State.NOT_SECURE;
            } else {
                this.lastSignature = param1;
                return SignedMessageValidator.State.SECURE;
            }
        }

        @Override
        public SignedMessageValidator.State validateHeader(SignedMessageHeader param0, MessageSignature param1, byte[] param2) {
            return this.updateAndValidate(param0, param1, param2);
        }

        @Override
        public SignedMessageValidator.State validateMessage(PlayerChatMessage param0) {
            byte[] var0 = param0.signedBody().hash().asBytes();
            return this.updateAndValidate(param0.signedHeader(), param0.headerSignature(), var0);
        }
    }

    public static enum State {
        SECURE,
        NOT_SECURE,
        BROKEN_CHAIN;
    }
}

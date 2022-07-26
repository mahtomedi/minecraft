package net.minecraft.network.chat;

import javax.annotation.Nullable;
import net.minecraft.util.SignatureValidator;
import net.minecraft.world.entity.player.ProfilePublicKey;

public interface SignedMessageValidator {
    static SignedMessageValidator create(@Nullable ProfilePublicKey param0, boolean param1) {
        return (SignedMessageValidator)(param0 != null
            ? new SignedMessageValidator.KeyBased(param0.createSignatureValidator())
            : new SignedMessageValidator.Unsigned(param1));
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

        private boolean validateChain(SignedMessageHeader param0, MessageSignature param1, boolean param2) {
            if (param1.isEmpty()) {
                return false;
            } else if (param2 && param1.equals(this.lastSignature)) {
                return true;
            } else {
                return this.lastSignature == null || this.lastSignature.equals(param0.previousSignature());
            }
        }

        private boolean validateContents(SignedMessageHeader param0, MessageSignature param1, byte[] param2, boolean param3) {
            return this.validateChain(param0, param1, param3) && param1.verify(this.validator, param0, param2);
        }

        private SignedMessageValidator.State updateAndValidate(SignedMessageHeader param0, MessageSignature param1, byte[] param2, boolean param3) {
            this.isChainConsistent = this.isChainConsistent && this.validateContents(param0, param1, param2, param3);
            if (!this.isChainConsistent) {
                return SignedMessageValidator.State.BROKEN_CHAIN;
            } else {
                this.lastSignature = param1;
                return SignedMessageValidator.State.SECURE;
            }
        }

        @Override
        public SignedMessageValidator.State validateHeader(SignedMessageHeader param0, MessageSignature param1, byte[] param2) {
            return this.updateAndValidate(param0, param1, param2, false);
        }

        @Override
        public SignedMessageValidator.State validateMessage(PlayerChatMessage param0) {
            byte[] var0 = param0.signedBody().hash().asBytes();
            return this.updateAndValidate(param0.signedHeader(), param0.headerSignature(), var0, true);
        }
    }

    public static enum State {
        SECURE,
        NOT_SECURE,
        BROKEN_CHAIN;
    }

    public static class Unsigned implements SignedMessageValidator {
        private final boolean enforcesSecureChat;

        public Unsigned(boolean param0) {
            this.enforcesSecureChat = param0;
        }

        private SignedMessageValidator.State validate(MessageSignature param0) {
            if (!param0.isEmpty()) {
                return SignedMessageValidator.State.BROKEN_CHAIN;
            } else {
                return this.enforcesSecureChat ? SignedMessageValidator.State.BROKEN_CHAIN : SignedMessageValidator.State.NOT_SECURE;
            }
        }

        @Override
        public SignedMessageValidator.State validateHeader(SignedMessageHeader param0, MessageSignature param1, byte[] param2) {
            return this.validate(param1);
        }

        @Override
        public SignedMessageValidator.State validateMessage(PlayerChatMessage param0) {
            return this.validate(param0.headerSignature());
        }
    }
}

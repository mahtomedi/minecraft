package net.minecraft.network.chat;

import javax.annotation.Nullable;
import net.minecraft.util.SignatureValidator;
import net.minecraft.world.entity.player.ProfilePublicKey;

public interface SignedMessageValidator {
    SignedMessageValidator ALWAYS_ACCEPT = new SignedMessageValidator() {
        @Override
        public void validateHeader(SignedMessageHeader param0, MessageSignature param1, byte[] param2) {
        }

        @Override
        public boolean validateMessage(PlayerChatMessage param0) {
            return true;
        }
    };

    static SignedMessageValidator create(@Nullable ProfilePublicKey param0) {
        return (SignedMessageValidator)(param0 != null ? new SignedMessageValidator.KeyBased(param0.createSignatureValidator()) : ALWAYS_ACCEPT);
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

        private boolean updateAndValidateChain(SignedMessageHeader param0, MessageSignature param1) {
            boolean var0 = this.lastSignature == null || this.lastSignature.equals(param0.previousSignature()) || this.lastSignature.equals(param1);
            this.lastSignature = param1;
            return var0;
        }

        @Override
        public void validateHeader(SignedMessageHeader param0, MessageSignature param1, byte[] param2) {
            boolean var0 = param1.verify(this.validator, param0, param2);
            boolean var1 = this.updateAndValidateChain(param0, param1);
            this.isChainConsistent = this.isChainConsistent && var0 && var1;
        }

        @Override
        public boolean validateMessage(PlayerChatMessage param0) {
            byte[] var0 = param0.signedBody().hash().asBytes();
            boolean var1 = param0.headerSignature().verify(this.validator, param0.signedHeader(), var0);
            boolean var2 = this.updateAndValidateChain(param0.signedHeader(), param0.headerSignature());
            boolean var3 = this.isChainConsistent && var1 && var2;
            this.isChainConsistent = var1;
            return var3;
        }
    }
}

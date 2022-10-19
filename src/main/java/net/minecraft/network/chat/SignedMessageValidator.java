package net.minecraft.network.chat;

import javax.annotation.Nullable;
import net.minecraft.util.SignatureValidator;

@FunctionalInterface
public interface SignedMessageValidator {
    SignedMessageValidator ACCEPT_UNSIGNED = param0 -> !param0.hasSignature();
    SignedMessageValidator REJECT_ALL = param0 -> false;

    boolean updateAndValidate(PlayerChatMessage var1);

    public static class KeyBased implements SignedMessageValidator {
        private final SignatureValidator validator;
        @Nullable
        private PlayerChatMessage lastMessage;
        private boolean isChainValid = true;

        public KeyBased(SignatureValidator param0) {
            this.validator = param0;
        }

        private boolean validateChain(PlayerChatMessage param0) {
            if (param0.equals(this.lastMessage)) {
                return true;
            } else {
                return this.lastMessage == null || param0.link().isDescendantOf(this.lastMessage.link());
            }
        }

        @Override
        public boolean updateAndValidate(PlayerChatMessage param0) {
            this.isChainValid = this.isChainValid && param0.verify(this.validator) && this.validateChain(param0);
            if (!this.isChainValid) {
                return false;
            } else {
                this.lastMessage = param0;
                return true;
            }
        }
    }
}

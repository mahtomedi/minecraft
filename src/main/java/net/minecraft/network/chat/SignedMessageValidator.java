package net.minecraft.network.chat;

import com.mojang.logging.LogUtils;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.util.SignatureValidator;
import org.slf4j.Logger;

@FunctionalInterface
public interface SignedMessageValidator {
    Logger LOGGER = LogUtils.getLogger();
    SignedMessageValidator ACCEPT_UNSIGNED = param0 -> {
        if (param0.hasSignature()) {
            LOGGER.error("Received chat message with signature from {}, but they have no chat session initialized", param0.sender());
            return false;
        } else {
            return true;
        }
    };
    SignedMessageValidator REJECT_ALL = param0 -> {
        LOGGER.error("Received chat message from {}, but they have no chat session initialized and secure chat is enforced", param0.sender());
        return false;
    };

    boolean updateAndValidate(PlayerChatMessage var1);

    public static class KeyBased implements SignedMessageValidator {
        private final SignatureValidator validator;
        private final BooleanSupplier expired;
        @Nullable
        private PlayerChatMessage lastMessage;
        private boolean isChainValid = true;

        public KeyBased(SignatureValidator param0, BooleanSupplier param1) {
            this.validator = param0;
            this.expired = param1;
        }

        private boolean validateChain(PlayerChatMessage param0) {
            if (param0.equals(this.lastMessage)) {
                return true;
            } else if (this.lastMessage != null && !param0.link().isDescendantOf(this.lastMessage.link())) {
                LOGGER.error(
                    "Received out-of-order chat message from {}: expected index > {} for session {}, but was {} for session {}",
                    param0.sender(),
                    this.lastMessage.link().index(),
                    this.lastMessage.link().sessionId(),
                    param0.link().index(),
                    param0.link().sessionId()
                );
                return false;
            } else {
                return true;
            }
        }

        private boolean validate(PlayerChatMessage param0) {
            if (this.expired.getAsBoolean()) {
                LOGGER.error("Received message from player with expired profile public key: {}", param0);
                return false;
            } else if (!param0.verify(this.validator)) {
                LOGGER.error("Received message with invalid signature from {}", param0.sender());
                return false;
            } else {
                return this.validateChain(param0);
            }
        }

        @Override
        public boolean updateAndValidate(PlayerChatMessage param0) {
            this.isChainValid = this.isChainValid && this.validate(param0);
            if (!this.isChainValid) {
                return false;
            } else {
                this.lastMessage = param0;
                return true;
            }
        }
    }
}

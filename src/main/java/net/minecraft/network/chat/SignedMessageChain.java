package net.minecraft.network.chat;

import com.mojang.logging.LogUtils;
import java.time.Instant;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.util.SignatureUpdater;
import net.minecraft.util.SignatureValidator;
import net.minecraft.util.Signer;
import net.minecraft.world.entity.player.ProfilePublicKey;
import org.slf4j.Logger;

public class SignedMessageChain {
    private static final Logger LOGGER = LogUtils.getLogger();
    @Nullable
    private SignedMessageLink nextLink;

    public SignedMessageChain(UUID param0, UUID param1) {
        this.nextLink = SignedMessageLink.root(param0, param1);
    }

    public SignedMessageChain.Encoder encoder(Signer param0) {
        return param1 -> {
            SignedMessageLink var0 = this.advanceLink();
            return var0 == null
                ? null
                : new MessageSignature(param0.sign((SignatureUpdater)(param2 -> PlayerChatMessage.updateSignature(param2, var0, param1))));
        };
    }

    public SignedMessageChain.Decoder decoder(ProfilePublicKey param0) {
        SignatureValidator var0 = param0.createSignatureValidator();
        return (param2, param3) -> {
            SignedMessageLink var0x = this.advanceLink();
            if (var0x == null) {
                throw new SignedMessageChain.DecodeException(Component.translatable("chat.disabled.chain_broken"), false);
            } else if (param0.data().hasExpired()) {
                throw new SignedMessageChain.DecodeException(Component.translatable("chat.disabled.expiredProfileKey"), false);
            } else {
                PlayerChatMessage var1x = new PlayerChatMessage(var0x, param2, param3, null, FilterMask.PASS_THROUGH);
                if (!var1x.verify(var0)) {
                    throw new SignedMessageChain.DecodeException(Component.translatable("multiplayer.disconnect.unsigned_chat"), true);
                } else {
                    if (var1x.hasExpiredServer(Instant.now())) {
                        LOGGER.warn("Received expired chat: '{}'. Is the client/server system time unsynchronized?", param3.content());
                    }

                    return var1x;
                }
            }
        };
    }

    @Nullable
    private SignedMessageLink advanceLink() {
        SignedMessageLink var0 = this.nextLink;
        if (var0 != null) {
            this.nextLink = var0.advance();
        }

        return var0;
    }

    public static class DecodeException extends ThrowingComponent {
        private final boolean shouldDisconnect;

        public DecodeException(Component param0, boolean param1) {
            super(param0);
            this.shouldDisconnect = param1;
        }

        public boolean shouldDisconnect() {
            return this.shouldDisconnect;
        }
    }

    @FunctionalInterface
    public interface Decoder {
        SignedMessageChain.Decoder REJECT_ALL = (param0, param1) -> {
            throw new SignedMessageChain.DecodeException(Component.translatable("chat.disabled.missingProfileKey"), false);
        };

        static SignedMessageChain.Decoder unsigned(UUID param0) {
            return (param1, param2) -> PlayerChatMessage.unsigned(param0, param2.content());
        }

        PlayerChatMessage unpack(@Nullable MessageSignature var1, SignedMessageBody var2) throws SignedMessageChain.DecodeException;
    }

    @FunctionalInterface
    public interface Encoder {
        SignedMessageChain.Encoder UNSIGNED = param0 -> null;

        @Nullable
        MessageSignature pack(SignedMessageBody var1);
    }
}

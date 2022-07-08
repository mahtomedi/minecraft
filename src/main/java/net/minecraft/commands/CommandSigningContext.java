package net.minecraft.commands;

import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MessageSigner;
import net.minecraft.network.chat.SignedMessageChain;

public interface CommandSigningContext {
    static CommandSigningContext anonymous() {
        final MessageSigner var0 = MessageSigner.system();
        return new CommandSigningContext() {
            @Override
            public MessageSignature getArgumentSignature(String param0) {
                return MessageSignature.EMPTY;
            }

            @Override
            public MessageSigner argumentSigner() {
                return var0;
            }

            @Override
            public boolean signedArgumentPreview(String param0) {
                return false;
            }

            @Override
            public SignedMessageChain.Decoder decoder() {
                return SignedMessageChain.Decoder.UNSIGNED;
            }
        };
    }

    MessageSignature getArgumentSignature(String var1);

    MessageSigner argumentSigner();

    SignedMessageChain.Decoder decoder();

    boolean signedArgumentPreview(String var1);

    public static record SignedArguments(
        SignedMessageChain.Decoder decoder, MessageSigner argumentSigner, ArgumentSignatures argumentSignatures, boolean signedPreview
    ) implements CommandSigningContext {
        @Override
        public MessageSignature getArgumentSignature(String param0) {
            return this.argumentSignatures.get(param0);
        }

        @Override
        public boolean signedArgumentPreview(String param0) {
            return this.signedPreview;
        }
    }
}

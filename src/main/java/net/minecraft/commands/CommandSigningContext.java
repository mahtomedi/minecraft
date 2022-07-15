package net.minecraft.commands;

import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MessageSigner;
import net.minecraft.network.chat.SignedMessageChain;

public interface CommandSigningContext {
    static CommandSigningContext anonymous() {
        final MessageSigner var0 = MessageSigner.system();
        return new CommandSigningContext() {
            @Override
            public CommandSigningContext.SignedArgument getArgument(String param0) {
                return CommandSigningContext.SignedArgument.UNSIGNED;
            }

            @Override
            public MessageSigner argumentSigner() {
                return var0;
            }

            @Override
            public SignedMessageChain.Decoder decoder() {
                return SignedMessageChain.Decoder.UNSIGNED;
            }
        };
    }

    CommandSigningContext.SignedArgument getArgument(String var1);

    MessageSigner argumentSigner();

    SignedMessageChain.Decoder decoder();

    public static record SignedArgument(MessageSignature signature, boolean signedPreview, LastSeenMessages lastSeenMessages) {
        public static final CommandSigningContext.SignedArgument UNSIGNED = new CommandSigningContext.SignedArgument(
            MessageSignature.EMPTY, false, LastSeenMessages.EMPTY
        );
    }

    public static record SignedArguments(
        SignedMessageChain.Decoder decoder,
        MessageSigner argumentSigner,
        ArgumentSignatures argumentSignatures,
        boolean signedPreview,
        LastSeenMessages lastSeenMessages
    ) implements CommandSigningContext {
        @Override
        public CommandSigningContext.SignedArgument getArgument(String param0) {
            return new CommandSigningContext.SignedArgument(this.argumentSignatures.get(param0), this.signedPreview, this.lastSeenMessages);
        }
    }
}

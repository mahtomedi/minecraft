package net.minecraft.commands;

import java.time.Instant;
import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.util.Crypt;

public interface CommandSigningContext {
    CommandSigningContext NONE = param0 -> MessageSignature.unsigned(Util.NIL_UUID);

    MessageSignature getArgumentSignature(String var1);

    default boolean signedArgumentPreview(String param0) {
        return false;
    }

    public static record SignedArguments(UUID sender, Instant timeStamp, ArgumentSignatures argumentSignatures, boolean signedPreview)
        implements CommandSigningContext {
        @Override
        public MessageSignature getArgumentSignature(String param0) {
            Crypt.SaltSignaturePair var0 = this.argumentSignatures.get(param0);
            return var0 != null ? new MessageSignature(this.sender, this.timeStamp, var0) : MessageSignature.unsigned(this.sender);
        }

        @Override
        public boolean signedArgumentPreview(String param0) {
            return this.signedPreview;
        }
    }
}

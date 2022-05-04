package net.minecraft.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.time.Instant;
import java.util.UUID;
import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.SignedMessage;
import net.minecraft.util.Crypt;

public interface CommandSigningContext {
    CommandSigningContext NONE = (param0, param1, param2) -> new SignedMessage(param2, MessageSignature.unsigned());

    SignedMessage signArgument(CommandContext<CommandSourceStack> var1, String var2, Component var3) throws CommandSyntaxException;

    public static record PlainArguments(UUID sender, Instant timeStamp, ArgumentSignatures argumentSignatures) implements CommandSigningContext {
        @Override
        public SignedMessage signArgument(CommandContext<CommandSourceStack> param0, String param1, Component param2) {
            Crypt.SaltSignaturePair var0 = this.argumentSignatures.get(param1);
            return var0 != null
                ? new SignedMessage(param2, new MessageSignature(this.sender, this.timeStamp, var0))
                : new SignedMessage(param2, MessageSignature.unsigned());
        }
    }
}
